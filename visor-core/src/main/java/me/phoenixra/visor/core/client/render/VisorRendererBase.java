package me.phoenixra.visor.core.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.VisorRenderer;
import me.phoenixra.visor.api.client.render.context.RenderContext;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.gui.GuiManagerImpl;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.provider.VisorScene;
import me.phoenixra.visor.core.client.render.target.impl.RenderTargetFirst;
import me.phoenixra.visor.core.client.render.target.impl.RenderTargetGUI;
import me.phoenixra.visor.core.client.render.target.impl.RenderTargetMain;
import me.phoenixra.visor.core.client.render.target.impl.RenderTargetThird;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public abstract class VisorRendererBase implements VisorRenderer {
    public RenderTargetMain mainTarget;

    public RenderTargetGUI guiTarget;
    public RenderTargetFirst firstPersonTarget;
    public RenderTargetThird thirdPersonTarget;

    private final Matrix4f[] eyeProjection = new Matrix4f[2];

    protected final Map<EyeType, float[]> hiddenArea = new HashMap<>();


    @Getter
    protected int resolutionWidth;
    @Getter
    protected int resolutionHeight;

    @Getter
    private int mirrorWidth;
    @Getter
    private int mirrorHeight;

    public float renderScale;


    protected MirrorMode lastMirror;
    public long lastWindow = 0L;


    @Getter @Setter
    private boolean askedForScreenShot = false;



    protected boolean reinitTargets = true;
    protected boolean resizeTargets = false;



    public VisorRendererBase() {
        hiddenArea.put(EyeType.LEFT, new float[0]);
        hiddenArea.put(EyeType.RIGHT, new float[0]);
        ClientContext.renderer = this;

    }



    protected abstract void setupEyes();
    protected abstract void setupResolution(MemoryStack stack);
    protected abstract void setupHiddenArea(MemoryStack stack);
    public abstract Matrix4f getProjectionMatrix(EyeType eyeType, float nearClip, float farClip);
    @Override
    public abstract VisorScene getCurrentScene();



    public void render(RenderContext context) {
        getCurrentScene()
                .updateRenderData(context.renderLevel(), context.nanoTime());

        renderFrame(context);

        GLUtils.checkGLError("vr render");
    }

    public void onGameRenderStart(boolean renderLevel) {

        try {
            GLUtils.checkGLError("pre render setup ");
            ClientContext.renderer.updateState();
            GLUtils.checkGLError("post render setup ");
        } catch (Throwable throwable) {
            VisorState.destroyVRWithError(throwable);
            return;
        }

        VRRenderState.startVRGuiPhase();

        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);

        MC.mainRenderTarget.clear(Minecraft.ON_OSX);
        MC.mainRenderTarget.bindWrite(true);

        // push pose to pop it in scene
        RenderSystem.getModelViewStack().pushPose();

    }

    public void updateState() throws Throwable {

        //Window context changed
        if (MC.getWindow().getWindow() != this.lastWindow) {
            this.lastWindow = MC.getWindow().getWindow();
            this.prepareReinit("Window Handle Changed");
        }

        //mirror mode
        if (this.lastMirror != VRClientSettings.getDisplayMirrorMode()) {
            this.prepareReinit("Mirror Changed");
            this.lastMirror = VRClientSettings.getDisplayMirrorMode();
        }

        //-----------------


        if (this.resizeTargets && !this.reinitTargets) {
            resizeTargets();
        }

        if (this.reinitTargets) {
            createTargets();
        }
    }



    @Override
    public void init() throws Throwable {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            setupResolution(stack);
            setupEyes();
            setupHiddenArea(stack);

        }
        updateState();
    }


    public void createTargets() throws Throwable {
        destroy();
        GLUtils.checkGLError("destroy on create");

        Minecraft minecraft = Minecraft.getInstance();
        int eyeWidth = getResolutionWidth();
        int eyeHeight = getResolutionHeight();

        this.renderScale = (float) Math.sqrt(VRClientSettings.getRenderScaleFactor());
        int eyeRenderWidth = (int) Math.ceil(eyeWidth * this.renderScale);
        int eyeRenderHeight = (int) Math.ceil(eyeHeight * this.renderScale);


        updateMirrorSize(eyeRenderWidth, eyeRenderHeight);

        mainTarget = new RenderTargetMain();
        mainTarget.init(
                eyeRenderWidth, eyeRenderHeight
        );

        List<VRDisplay> list = getVRWorldDisplays();
        for (VRDisplay renderStage : list) {
            VisorClientImpl.LOGGER.info("Passes: " + renderStage.toString());
        }

        firstPersonTarget = new RenderTargetFirst();

        if(list.contains(VRDisplay.FIRST_PERSON)
                || ShadersHelper.isShaderActive()
                && (mirrorWidth > 0 && mirrorHeight > 0)) {
            firstPersonTarget.init(
                    mirrorWidth, mirrorHeight
            );
        }


        thirdPersonTarget = new RenderTargetThird();
        if(list.contains(VRDisplay.THIRD_PERSON)
                || ShadersHelper.isShaderActive()
                && (mirrorWidth > 0 && mirrorHeight > 0)) {
            thirdPersonTarget.init(
                    mirrorWidth, mirrorHeight
            );
        }


        GuiManagerImpl guiManager = ClientContext.visor.getGuiManager();
        guiManager.updateResolution();
        guiTarget = new RenderTargetGUI();
        guiTarget.init(
                guiManager.getGuiWidth(),
                guiManager.getGuiHeight()
        );


        ((GameRendererModified) minecraft.gameRenderer)
                .visor$setupClipPlanes();
        updateProjection();

        try {
            minecraft.mainRenderTarget = mainTarget.getTarget();

            VRShaders.setup();
        } catch (Exception exception1) {
            LoggerUtils.printError(exception1);
            System.exit(-1);
        }

        if (minecraft.screen != null) {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            minecraft.screen.init(minecraft, screenWidth, screenHeight);
        }

        var windowModif = (WindowModified) (Object) minecraft.getWindow();
        long windowPixels = (long) windowModif.visor$getScreenWidth() * windowModif.visor$getScreenHeight();
        long vrPixels = eyeRenderWidth * eyeRenderHeight * 2L;

        if (list.contains(VRDisplay.FIRST_PERSON)) {
            vrPixels += windowPixels;
        }


        VisorClientImpl.LOGGER.info("[Visor] render targets created:" +
                "\nEye target width: " + eyeWidth + ", height: " + eyeHeight + " [" + String.format("%.1f", (float) (eyeWidth * eyeHeight) / 1000000.0F) + " MP]" +
                "\nRender target width: " + eyeRenderWidth + ", height: " + eyeRenderHeight + " [Render scale: " + Math.round(VRClientSettings.getRenderScaleFactor() * 100.0F) + "%, " + String.format("%.1f", (float) (eyeRenderWidth * eyeRenderHeight) / 1000000.0F) + " MP]" +
                "\nMain window width: " + windowModif.visor$getScreenWidth() + ", height: " + windowModif.visor$getScreenHeight() + " [" + String.format("%.1f", (float) windowPixels / 1000000.0F) + " MP]" +
                "\nTotal shaded pixels per frame: " + String.format("%.1f", (float) vrPixels / 1000000.0F) + " MP (eye stencil not accounted for)");
        this.reinitTargets = false;

    }

    private void resizeTargets() throws Exception {
        resizeTargets = false;

        float resolutionScale = 1.0F;

        this.renderScale = (float) Math.sqrt(VRClientSettings.getRenderScaleFactor()) * resolutionScale;
        int eyeRenderWidth = (int) Math.ceil(getResolutionWidth() * this.renderScale);
        int eyeRenderHeight = (int) Math.ceil(getResolutionHeight() * this.renderScale);

        updateMirrorSize(eyeRenderWidth, eyeRenderHeight);

        // main render target
        mainTarget.resize(eyeRenderWidth, eyeRenderHeight);

        // mirror
        if (firstPersonTarget != null) {
            firstPersonTarget.resize(mirrorWidth, mirrorHeight);
        }
        if (thirdPersonTarget != null) {
            thirdPersonTarget.resize(mirrorWidth, mirrorHeight);
        }


        // resize gui, if changed
        GuiManagerImpl guiManager = ClientContext.visor.getGuiManager();
        if (guiManager.updateResolution()) {
            guiTarget.resize(
                    guiManager.getGuiWidth(),
                    guiManager.getGuiHeight()
            );
        }
    }


    @Override
    public void prepareReinit(@NotNull String cause) {
        if (!reinitTargets) {
            // only print the initial cause
            VisorClientImpl.LOGGER.info("Reinit Render Buffers: {}", cause);
        }
        this.reinitTargets = true;
    }

    @Override
    public void prepareResize(@NotNull String cause) {
        if (!this.resizeTargets) {
            // only print the initial cause
            VisorClientImpl.LOGGER.info("Resizing Render Buffers: {}", cause);
        }
        this.resizeTargets = true;
    }


    public Matrix4f getEyeProjection(EyeType eyeType) {
        return eyeProjection[eyeType.getIndex()];
    }

    public void updateProjection() {
        float nearClipPlane = ((GameRendererModified) MC.gameRenderer)
                .visor$getNearClipPlane();
        float farClipPlane = ((GameRendererModified) MC.gameRenderer)
                .visor$getFarClipPlane();
        VRClientSettings.setEyeFovChanged(false);
        this.eyeProjection[0] = this.getProjectionMatrix(EyeType.LEFT,
                nearClipPlane,
                farClipPlane
        );
        this.eyeProjection[1] = this.getProjectionMatrix(EyeType.RIGHT,
                nearClipPlane,
                farClipPlane
        );

    }

    private void updateMirrorSize(int eyeFBWidth, int eyeFBHeight) {
        var windowModif =  ((WindowModified) (Object)
                Minecraft.getInstance().getWindow());
        mirrorWidth = Math.max(1,
                windowModif.visor$getScreenWidth()
        );
        mirrorHeight = Math.max(1,
                windowModif.visor$getScreenHeight()
        );


        if (ShadersHelper.sameSizedBuffers()) {
            mirrorWidth = eyeFBWidth;
            mirrorHeight = eyeFBHeight;
        }
    }
    public static List<VRDisplay> getVRWorldDisplays() {


        List<VRDisplay> list = new ArrayList<>();
        list.add(VRDisplay.EYE_LEFT);
        list.add(VRDisplay.EYE_RIGHT);

        var windowModif =  ((WindowModified) (Object)
                Minecraft.getInstance().getWindow());

        if (windowModif.visor$getScreenWidth() > 0
                && windowModif.visor$getScreenHeight() > 0) {
            MirrorMode mirrorMode = VRClientSettings.getDisplayMirrorMode();
            if (mirrorMode == MirrorMode.FIRST_PERSON) {
                list.add(VRDisplay.FIRST_PERSON);
            } else if (mirrorMode == MirrorMode.THIRD_PERSON) {
                list.add(VRDisplay.THIRD_PERSON);
            }
        }

        return list;
    }

    @Override
    public void destroy() {
        if (mainTarget != null) {
            mainTarget.destroy();
        }
        if (firstPersonTarget != null) {
            firstPersonTarget.destroy();
        }
        if (thirdPersonTarget != null) {
            thirdPersonTarget.destroy();
        }
        if (guiTarget != null) {
            guiTarget.destroy();
            guiTarget = null;
        }
    }



    @Override
    public long getWindowHandle() {
        return MC.getWindow().getWindow();
    }

    @Override
    public float[] getHiddenAreaVertices(EyeType eyeType) {
        return hiddenArea.get(eyeType);
    }
}

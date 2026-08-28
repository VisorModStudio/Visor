package org.vmstudio.visor.core.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.VRRenderer;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.gui.VRGuiManagerImpl;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.provider.VisorScene;
import org.vmstudio.visor.core.client.render.target.types.RenderTargetFirst;
import org.vmstudio.visor.core.client.render.target.types.RenderTargetGUI;
import org.vmstudio.visor.core.client.render.target.types.RenderTargetMain;
import org.vmstudio.visor.core.client.render.target.types.RenderTargetThird;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vmstudio.visor.core.client.ClientContext;
import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public abstract class VRRendererBase implements VRRenderer {
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


    public long lastWindow = 0L;


    @Getter @Setter
    private boolean askedForScreenShot = false;



    protected boolean reinitTargets = true;
    protected boolean resizeTargets = false;



    public VRRendererBase() {
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
        ClientContext.decorationRenderer.updateRenderState();
        renderFrame(context);

    }

    public void onGameRenderStart(boolean renderLevel) {

        try {
            GLUtils.checkGLError("pre render setup ");
            ClientContext.renderer.updateState();
            GLUtils.checkGLError("post render setup ");
        } catch (Throwable throwable) {
            VisorState.destroyVRWithErrorScreen(throwable);
            return;
        }

        VRRenderState.startVRGuiPhase();

        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);

        MC.mainRenderTarget.clear(Minecraft.ON_OSX);
        MC.mainRenderTarget.bindWrite(true);

        // push pose to pop it in scene
        RenderSystem.getModelViewStack().pushPose();

        ((GameRendererExtension)MC.gameRenderer).visor$setVRGuiVisible(
                renderLevel && MC.getEntityRenderDispatcher().camera != null
        );
    }

    public void updateState() throws Throwable {

        //Window context changed
        if (MC.getWindow().getWindow() != this.lastWindow) {
            this.lastWindow = MC.getWindow().getWindow();
            this.prepareReinit("window recreated");
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

        List<VRRenderPass> list = VRRenderState.getActivePasses();
        for (VRRenderPass renderStage : list) {
            VisorClientImpl.LOGGER.info("VR Displays: {}", renderStage.toString());
        }

        updateMirrorSize(eyeRenderWidth, eyeRenderHeight);

        mainTarget = new RenderTargetMain();
        mainTarget.init(
                eyeRenderWidth, eyeRenderHeight
        );

        firstPersonTarget = new RenderTargetFirst();
        if(list.contains(VRRenderPass.CENTER)
                || ShaderCompatHelper.isShaderActive()
                && (mirrorWidth > 0 && mirrorHeight > 0)) {
            firstPersonTarget.init(
                    mirrorWidth, mirrorHeight
            );
        }

        thirdPersonTarget = new RenderTargetThird();
        if(list.contains(VRRenderPass.THIRD_PERSON)
                || ShaderCompatHelper.isShaderActive()
                && (mirrorWidth > 0 && mirrorHeight > 0)) {
            thirdPersonTarget.init(
                    mirrorWidth, mirrorHeight
            );
        }


        VRGuiManagerImpl guiManager = ClientContext.visor.getGuiManager();
        guiManager.updateResolution();
        guiTarget = new RenderTargetGUI();
        guiTarget.init(
                guiManager.getGuiWidth(),
                guiManager.getGuiHeight()
        );


        ((GameRendererExtension) minecraft.gameRenderer)
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
            minecraft.resizeDisplay();
        }

        var windowModif = (WindowExtension) (Object) minecraft.getWindow();
        long windowPixels = (long) windowModif.visor$mcScreenWidth() * windowModif.visor$mcScreenHeight();
        long vrPixels = eyeRenderWidth * eyeRenderHeight * 2L;

        if (list.contains(VRRenderPass.CENTER)) {
            vrPixels += windowPixels;
        }


        VisorClientImpl.LOGGER.info(
                "[Visor] render targets created:" +
                "\nEye target: " + eyeWidth + "x" + eyeHeight + " (" + String.format("%.1f", (float) (eyeWidth * eyeHeight) / 1000000.0F) + " MP)" +
                "\nRender target: " + eyeRenderWidth + "x" + eyeRenderHeight + " at " + Math.round(VRClientSettings.getRenderScaleFactor() * 100.0F) + "% scale (" + String.format("%.1f", (float) (eyeRenderWidth * eyeRenderHeight) / 1000000.0F) + " MP)" +
                "\nMain window: " + windowModif.visor$mcScreenWidth() + "x" + windowModif.visor$mcScreenHeight() + " (" + String.format("%.1f", (float) windowPixels / 1000000.0F) + " MP)" +
                "\nPer-frame fill: " + String.format("%.1f", (float) vrPixels / 1000000.0F) + " MP, stencil mask not subtracted"
        );

        minecraft.levelRenderer.onResourceManagerReload(minecraft.getResourceManager());

        ShaderCompatHelper.bridge().onVisorTargetsRecreated(eyeRenderWidth, eyeRenderHeight);

        this.reinitTargets = false;


    }

    private void resizeTargets() throws Exception {
        resizeTargets = false;

        this.renderScale = (float) Math.sqrt(VRClientSettings.getRenderScaleFactor());
        int eyeRenderWidth = (int) Math.ceil(getResolutionWidth() * this.renderScale);
        int eyeRenderHeight = (int) Math.ceil(getResolutionHeight() * this.renderScale);

        updateMirrorSize(eyeRenderWidth, eyeRenderHeight);
        mainTarget.resize(eyeRenderWidth, eyeRenderHeight);

        // mirror
        if (firstPersonTarget != null) {
            firstPersonTarget.resize(mirrorWidth, mirrorHeight);
        }
        if (thirdPersonTarget != null) {
            thirdPersonTarget.resize(mirrorWidth, mirrorHeight);
        }

        VRGuiManagerImpl guiManager = ClientContext.visor.getGuiManager();
        if (guiManager.updateResolution()) {
            guiTarget.resize(
                    guiManager.getGuiWidth(),
                    guiManager.getGuiHeight()
            );
        }

        Minecraft.getInstance().resizeDisplay();
    }


    @Override
    public void prepareReinit(@NotNull String cause) {
        if (!reinitTargets) {
            VisorClientImpl.LOGGER.info("Reinit Render Buffers: {}", cause);
        }
        this.reinitTargets = true;
    }

    @Override
    public void prepareResize(@NotNull String cause) {
        if (!this.resizeTargets) {
            VisorClientImpl.LOGGER.info("Resizing Render Buffers: {}", cause);
        }
        this.resizeTargets = true;
    }


    public Matrix4f getEyeProjection(EyeType eyeType) {
        return eyeProjection[eyeType.getIndex()];
    }

    public void updateProjection() {
        float nearClipPlane = ((GameRendererExtension) MC.gameRenderer)
                .visor$getNearClipPlane();
        float farClipPlane = ((GameRendererExtension) MC.gameRenderer)
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

    private void updateMirrorSize(int eyeWidth, int eyeHeight) {
        var windowModif = ((WindowExtension) (Object)
                Minecraft.getInstance().getWindow());
        mirrorWidth = Math.max(1,
                windowModif.visor$mcScreenWidth()
        );
        mirrorHeight = Math.max(1,
                windowModif.visor$mcScreenHeight()
        );
        if (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY) {
            mirrorWidth = mirrorWidth / 2;

            if (VRClientSettings.isMixedRealityAsGrid2x2()) {
                mirrorHeight = mirrorHeight / 2;
            }
        }

        if (ShaderCompatHelper.sameSizedBuffers()) {
            mirrorWidth = eyeWidth;
            mirrorHeight = eyeHeight;
        }
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
    public void updateOverlayTarget(@NotNull VROverlayScreen overlayScreen) {
        if(guiTarget == null) return;
        guiTarget.updateOverlayTarget(overlayScreen);
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

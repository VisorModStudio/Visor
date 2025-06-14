package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.OverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.OverlayCatalog;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.gui.overlays.types.VROverlayHUD;
import me.phoenixra.visor.core.client.gui.registry.VROverlayRegistry;
import me.phoenixra.visor.core.client.gui.registry.VROverlayTypeRegistry;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenGlobal;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenModelView;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.RenderGuiHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class OverlayManagerImpl implements OverlayManager {

    @Getter
    private final VROverlayRegistry overlaysRegistry = new VROverlayRegistry();
    @Getter
    private final VROverlayTypeRegistry overlayTypesRegistry = new VROverlayTypeRegistry();


    private VROverlayKeyboard keyboard;

    @Getter
    private boolean hudDisplayed;


    public void tick(){
        for(VROverlay overlay : overlaysRegistry.getSortedElements()){
            if(!overlay.isEnabled()) continue;
            overlay.tick();
        }

        hudDisplayed = overlaysRegistry.getElementsByType(
                VROverlayHUD.ID_TYPE
        ).stream().anyMatch(VROverlay::isVisible);

    }

    public void renderOverlayTextures(ProfilerFiller profiler,
                                      GuiGraphics guiGraphics,
                                      float partialTicks) {

        //--- Setup ---
        Matrix4f projection = new Matrix4f();
        int prevOverlayWidth = -1;
        int prevOverlayHeight = -1;

        RenderSystem.backupProjectionMatrix();

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, -11000.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE
        );


        //--- Render ---
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;

            profiler.push("VROverlay Texture: " + overlay.getId());

            if(overlay instanceof VROverlayScreen overlayScreen) {

                RenderTarget target = overlay.getRenderTarget();
                if(target == null){
                    throw new RuntimeException("Tried to render overlay with null renderTarget");
                }

                //apply clean render target
                MC.mainRenderTarget = target;
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);

                //setup projection
                if(prevOverlayWidth != overlayScreen.width
                        || prevOverlayHeight != overlayScreen.height) {
                    projection.setOrtho(
                            0,
                            overlayScreen.width, overlayScreen.height,
                            0,
                            1000.0F, 21000.0F
                    );
                    RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
                    prevOverlayWidth = overlayScreen.width;
                    prevOverlayHeight = overlayScreen.height;
                }

                //render overlay texture
                overlayScreen.renderWithTooltip(
                        guiGraphics,
                        overlayScreen.getMouseX(),
                        overlayScreen.getMouseY(),
                        partialTicks
                );
                guiGraphics.flush();

            }else if(overlay instanceof VROverlayFrameBuffer overlayFrameBuffer){
                // renderTarget is fully handled by VROverlayFrameBuffer,
                // so, just call render(), to let it do the rest
                overlayFrameBuffer.render(partialTicks);
            }

            profiler.pop();
        }

        //--- Restore ---
        RenderSystem.restoreProjectionMatrix();

        posestack.popPose();

    }

    public void renderOverlays(float partialTicks,
                               boolean depthAlways,
                               PoseStack poseStack) {
        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyDisplayOrientation(
                VRRenderState.getCurrentVRDisplay(),
                poseStack
        );
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;
            var target = overlay.getRenderTarget();
            if(target == null){
                throw new RuntimeException("Tried to render overlay with");
            }
            overlay.applyModelView(partialTicks);
            RenderGuiHelper.renderOverlayQuad(
                    target,
                    poseStack,
                    overlay.getPosition(),
                    overlay.getRotation(),
                    depthAlways,
                    overlay.getOverlayScale()
            );
        }
        poseStack.popPose();
    }

    @Override
    public boolean isEnabled(@NotNull String id) {
        VROverlay overlay = getOverlay(id);
        if (overlay == null) return false;
        return overlay.isEnabled();
    }

    @Override
    public boolean isEnabledAtLeastOne() {
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if (overlay.isEnabled()) return true;
        }
        return false;
    }


    @Override
    public VROverlay getOverlay(@NotNull String id) {
        return overlaysRegistry.getElement(id);
    }

    @Override
    public boolean isShowingKeyboard() {
        return getKeyboardOverlay().isShown();
    }

    @Override
    public void showKeyboard(boolean flag) {
        getKeyboardOverlay().showKeyboard(flag);
    }
    @Override
    public boolean showKeyboard(boolean flag,
                                @Nullable Screen attachedTo){
        return getKeyboardOverlay().showKeyboard(flag,attachedTo);
    }

    @Override
    public Screen getKeyboardAttachedTo() {
        return getKeyboardOverlay().getAttachedTo();
    }


    public VROverlayKeyboard getKeyboardOverlay() {
        if(keyboard == null) {
            keyboard = (VROverlayKeyboard) getOverlay("keyboard");
        }
        return keyboard;
    }

    @Override
    public @NotNull OverlayCatalog getOverlayCatalog() {
        return ClientContext.settingsHandler.getOverlayCatalog();
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionCategory category, float mainMenuWidth, float mainMenuHeight) {
        if(category instanceof OverlayOptionsGlobal category1){
            return new OptionsScreenGlobal(category1,mainMenuWidth,mainMenuHeight);
        }else if(category instanceof OverlayOptionsModelView category1){
            return new OptionsScreenModelView(category1, mainMenuWidth, mainMenuHeight);
        }
        return null; // tsss, secret
    }
}

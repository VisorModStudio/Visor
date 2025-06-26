package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.gui.VRKeyboardAccessor;
import me.phoenixra.visor.api.client.gui.VROverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.template.ConfigOverlaysAccessor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
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

@Getter
public class VROverlayManagerImpl implements VROverlayManager {

    private final VROverlayRegistry overlaysRegistry = new VROverlayRegistry();
    private final VROverlayTypeRegistry overlayTypesRegistry = new VROverlayTypeRegistry();


    @Setter
    private VRKeyboardAccessor keyboardAccessor;


    public void tick(){
        for(VROverlay overlay : overlaysRegistry.getSortedElements()){
            if(!overlay.isEnabled()) continue;
            overlay.tick();
        }


    }

    public void renderOverlayTextures(ProfilerFiller profiler,
                                      GuiGraphics guiGraphics,
                                      float partialTicks) {

        // --- Setup ---
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


        // --- Render ---
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

                //setup projection if changed
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

                //update pose before rendering texture
                overlay.updatePose(partialTicks);

                //render overlay texture
                overlayScreen.renderWithTooltip(
                        guiGraphics,
                        overlayScreen.getMouseX(),
                        overlayScreen.getMouseY(),
                        partialTicks
                );
                guiGraphics.flush();

            }else if(overlay instanceof VROverlayFrameBuffer overlayFrameBuffer){
                // rendering is fully handled by VROverlayFrameBuffer,
                // so, just call updatePose() and render(),
                // to let it do the rest
                overlay.updatePose(partialTicks);
                overlayFrameBuffer.render(partialTicks);
            }

            profiler.pop();
        }

        // --- Restore ---
        RenderSystem.restoreProjectionMatrix();

        posestack.popPose();

    }

    public void renderOverlays(float partialTicks,
                               PoseStack poseStack) {


        // --- Setup ---
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyDisplayOrientation(
                VRRenderState.getCurrentVRDisplay(),
                poseStack
        );

        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        // --- Render ---
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;
            var target = overlay.getRenderTarget();
            if(target == null){
                throw new RuntimeException("Tried to render overlay with null renderTarget");
            }

            RenderGuiHelper.renderOverlayQuad(
                    target,
                    poseStack,
                    overlay.getPose().getPosition(),
                    overlay.getPose().getRotation(),
                    !overlay.supportsDepth(),
                    overlay.getPose().getScale()
            );
        }

        // --- Restore ---
        poseStack.popPose();
    }



    @Override
    public VROverlay getOverlay(@NotNull String id) {
        return overlaysRegistry.getElement(id);
    }


    @Override
    public @NotNull ConfigOverlaysAccessor getConfigOverlaysAccessor() {
        return ClientContext.settingsHandler.getOverlaysAccessor();
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptions category, float mainMenuWidth, float mainMenuHeight) {
        if(category instanceof OverlayOptionsGlobal category1){
            return new OptionsScreenGlobal(category1,mainMenuWidth,mainMenuHeight);
        }else if(category instanceof OverlayOptionsLocation category1){
            return new OptionsScreenModelView(category1, mainMenuWidth, mainMenuHeight);
        }
        return null; // tsss, secret
    }
}

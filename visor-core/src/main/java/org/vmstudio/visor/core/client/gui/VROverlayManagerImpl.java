package org.vmstudio.visor.core.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.client.gui.VRKeyboardAccessor;
import org.vmstudio.visor.api.client.gui.VROverlayManager;
import org.vmstudio.visor.api.client.gui.OverlayConfigAccessor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayFrameBuffer;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.registry.VROverlayRegistry;
import org.vmstudio.visor.core.client.gui.registry.VROverlayTemplateRegistry;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.*;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderGuiHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.client.gui.overlays.options.types.*;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Getter
public class VROverlayManagerImpl implements VROverlayManager {

    private final VROverlayRegistry overlaysRegistry = new VROverlayRegistry();
    private final VROverlayTemplateRegistry overlayTemplatesRegistry = new VROverlayTemplateRegistry();


    @Setter
    private VRKeyboardAccessor keyboardAccessor;


    private final List<VROverlay> preparedOverlays = new ArrayList<>();

    private final List<VROverlay> preparedDepthOverlays = new ArrayList<>();
    private final List<VROverlay> preparedHudOverlays = new ArrayList<>();

    public void tick(){
        for(VROverlay overlay : overlaysRegistry.getSortedComponents()){
            if(!overlay.isEnabled()) continue;
            overlay.tick();
        }


    }

    public void prepareOverlaysAndCursor(float partialTicks){
        preparedOverlays.clear();
        preparedDepthOverlays.clear();
        preparedHudOverlays.clear();

        for (VROverlay overlay : overlaysRegistry.getSortedComponents()) {
            if(!overlay.isVisible()) continue;
            RenderTarget target = overlay.getRenderTarget();
            if(target == null){
                continue;
            }

            overlay.updatePose(partialTicks);

            //do not render overlay if out of view distance
            if(!overlay.isInViewDistance()){
                continue;
            }

            //ready to be rendered
            preparedOverlays.add(overlay);

            // Split into depth and HUD layer lists
            if (overlay.isHudLayer()) {
                preparedHudOverlays.add(overlay);
            } else {
                preparedDepthOverlays.add(overlay);
            }
        }
        ClientContext.cursorHandler.process();
    }

    public void renderOverlayTextures(ProfilerFiller profiler,
                                      GuiGraphics guiGraphics,
                                      float partialTicks) {
        if(preparedOverlays.isEmpty()){
            return;
        }
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

        // --- Render  ---
        for(var overlay : preparedOverlays){
            RenderTarget target = overlay.getRenderTarget();
            if(target == null){
                //shouldn't happen at all
                throw new RuntimeException("Tried to render overlay quad with null renderTarget: "+overlay.getId());
            }
            profiler.push("VROverlay Texture: " + overlay.getId());

            if(overlay instanceof VROverlayScreen overlayScreen) {
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
                // so, just render() is called,
                // and let it do the rest
                overlayFrameBuffer.render(partialTicks);
            }else{
                throw new RuntimeException("Tried to render overlay of unsupported abstract class: "+overlay.getId());
            }

            profiler.pop();
            GLUtils.checkGLError("post VROverlay texture: "+overlay.getId());
        }

        // --- Restore ---
        RenderSystem.restoreProjectionMatrix();

        posestack.popPose();

    }



    /**
     * Render only overlays that support depth (GL_LEQUAL).
     * These participate in proper depth testing with VR hands and world geometry.
     */
    public void renderDepthOverlays(float partialTicks,
                                    PoseStack poseStack) {
        if (preparedDepthOverlays.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(
                VRRenderState.getRenderPass(),
                poseStack
        );

        ((GameRendererExtension) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);
        GLUtils.checkGLError("before depth overlays");

        for (VROverlay overlay : preparedDepthOverlays) {
            if (!overlay.isVisible()) {
                continue;
            }
            var target = overlay.getRenderTarget();
            if (target == null) {
                throw new RuntimeException("Tried to render overlay quad with null renderTarget: " + overlay.getId());
            }

            boolean drawDragHandle = overlay.supportsDragging() &&
                    (overlay.isBeingDragged() || overlay.isBeingResized() ||
                            ((ClientContext.cursorHandler.getFocusedOverlay(HandType.MAIN,true) == overlay
                                    || ClientContext.cursorHandler.getFocusedOverlay(HandType.OFFHAND,true) == overlay)));

            RenderGuiHelper.renderOverlayQuad(
                    overlay,
                    poseStack,
                    overlay.getPose().getPosition(),
                    overlay.getPose().getRotation(),
                    false, // depthAlways = false, use GL_LEQUAL
                    overlay.supportsLight(),
                    drawDragHandle,
                    overlay.getPose().getScale()
            );
            GLUtils.checkGLError("post depth VROverlay quad: " + overlay.getId());
        }

        poseStack.popPose();
    }

    /**
     * Render only HUD overlays (no depth testing — GL_ALWAYS).
     * These render as a top layer, not occluded by world objects
     */
    public void renderHudOverlays(float partialTicks,
                                  PoseStack poseStack) {
        if (preparedHudOverlays.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(
                VRRenderState.getRenderPass(),
                poseStack
        );

        ((GameRendererExtension) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);
        GLUtils.checkGLError("before hud overlays");

        for (VROverlay overlay : preparedHudOverlays) {
            if (!overlay.isVisible()) {
                continue;
            }
            var target = overlay.getRenderTarget();
            if (target == null) {
                throw new RuntimeException("Tried to render overlay quad with null renderTarget: " + overlay.getId());
            }

            boolean drawDragHandle = overlay.supportsDragging() &&
                    (overlay.isBeingDragged() || overlay.isBeingResized() ||
                            ((ClientContext.cursorHandler.getFocusedOverlay(HandType.MAIN,true) == overlay
                                    || ClientContext.cursorHandler.getFocusedOverlay(HandType.OFFHAND,true) == overlay)));
            RenderGuiHelper.renderOverlayQuad(
                    overlay,
                    poseStack,
                    overlay.getPose().getPosition(),
                    overlay.getPose().getRotation(),
                    true, // depthAlways = true, use GL_ALWAYS
                    overlay.supportsLight(),
                    drawDragHandle,
                    overlay.getPose().getScale()
            );

            GLUtils.checkGLError("post hud VROverlay quad: " + overlay.getId());
        }

        poseStack.popPose();
    }


    @Override
    public VROverlay getOverlay(@NotNull String id) {
        return overlaysRegistry.getComponent(id);
    }


    @Override
    public @NotNull OverlayConfigAccessor getOverlayConfigAccessor() {
        return ClientContext.settingsManager.getOverlayConfigsAccessor();
    }

    @Override
    public @NotNull OptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionGroup<?> category) {
        if(category instanceof OverlayOptionsMisc type){
            return new OptionsScreenMisc(type);
        }
        else if(category instanceof OverlayOptionsPose type){
            return new OptionsScreenPose(type);
        }
        else if(category instanceof OverlayOptionsIdentity type){
            return new OptionsScreenIdentity(type);
        }
        else if(category instanceof OverlayOptionsGeneral type){
            return new OptionsScreenGeneral(type);
        }
        else if(category instanceof OverlayOptionsScreenRegion type){
            return new OptionsScreenRegion(type);
        }else if(category instanceof OverlayOptionsVisibility type){
            return new OptionsScreenVisibility(type);
        }
        return null;
    }
}
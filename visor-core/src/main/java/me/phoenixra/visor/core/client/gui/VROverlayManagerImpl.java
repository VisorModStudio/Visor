package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.api.client.gui.VRKeyboardAccessor;
import me.phoenixra.visor.api.client.gui.VROverlayManager;
import me.phoenixra.visor.api.client.gui.overlays.OverlayConfigsAccessor;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsProperties;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.registry.VROverlayRegistry;
import me.phoenixra.visor.core.client.gui.registry.VROverlayTemplateRegistry;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenGlobal;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenIdentity;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenProperties;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.helpers.RenderGuiHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Getter
public class VROverlayManagerImpl implements VROverlayManager {

    private final VROverlayRegistry overlaysRegistry = new VROverlayRegistry();
    private final VROverlayTemplateRegistry overlayTemplatesRegistry = new VROverlayTemplateRegistry();


    @Setter
    private VRKeyboardAccessor keyboardAccessor;


    private final List<VROverlay> preparedOverlays = new ArrayList<>();
    public void tick(){
        for(VROverlay overlay : overlaysRegistry.getSortedElements()){
            if(!overlay.isEnabled()) continue;
            overlay.tick();
        }


    }

    public void prepareOverlaysAndCursor(float partialTicks){
        preparedOverlays.clear();
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;
            RenderTarget target = overlay.getRenderTarget();

            //make sure renderTarget exists
            if(target == null){
                continue;
            }

            //update pose
            overlay.updatePose(partialTicks);

            //do not render overlay if out of view distance
            if(!overlay.isInViewDistance()){
                continue;
            }

            //ready to be rendered
            preparedOverlays.add(overlay);
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
                // so, just call render(),
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

    public void renderOverlays(float partialTicks,
                               PoseStack poseStack) {

        if(preparedOverlays.isEmpty()){
            return;
        }
        // --- Setup ---
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyDisplayOrientation(
                VRRenderState.getCurrentVRDisplay(),
                poseStack
        );

        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);
        GLUtils.checkGLError("before overlays");
        // --- Render ---
        for (VROverlay overlay : preparedOverlays) {
            if(!overlay.isVisible()){
                continue;
            }
            var target = overlay.getRenderTarget();
            if(target == null){
                //shouldn't happen at all
                throw new RuntimeException("Tried to render overlay quad with null renderTarget: "+overlay.getId());
            }

            RenderGuiHelper.renderOverlayQuad(
                    overlay,
                    poseStack,
                    overlay.getPose().getPosition(),
                    overlay.getPose().getRotation(),
                    !overlay.supportsDepth(),
                    overlay.supportsLight(),
                    overlay.getPose().getScale()
            );
            GLUtils.checkGLError("post VROverlay quad: "+overlay.getId());
        }

        // --- Restore ---
        poseStack.popPose();
    }



    @Override
    public VROverlay getOverlay(@NotNull String id) {
        return overlaysRegistry.getElement(id);
    }


    @Override
    public @NotNull OverlayConfigsAccessor getConfigOverlaysAccessor() {
        return ClientContext.settingsHandler.getOverlayConfigsAccessor();
    }

    @Override
    public @NotNull OptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionGroup<?> category) {
        if(category instanceof OverlayOptionsGlobal type){
            return new OptionsScreenGlobal(type);
        }
        else if(category instanceof OverlayOptionsPose type){
            return new OptionsScreenPose(type);
        }
        else if(category instanceof OverlayOptionsIdentity type){
            return new OptionsScreenIdentity(type);
        }
        else if(category instanceof OverlayOptionsProperties type){
            return new OptionsScreenProperties(type);
        }
        return null;
    }
}

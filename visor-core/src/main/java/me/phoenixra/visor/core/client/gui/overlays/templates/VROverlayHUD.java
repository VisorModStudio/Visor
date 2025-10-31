package me.phoenixra.visor.core.client.gui.overlays.templates;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.framework.template.VROverlayTemplateFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsScreenRegion;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVROverlayTemplate(
        id = VROverlayHUD.ID,
        name = VROverlayHUD.NAME,
        description = VROverlayHUD.DESCRIPTION,
        isCreateDefault = true
)
public class VROverlayHUD extends VROverlayTemplateFrameBuffer implements VREventListener {
    public static final String ID = "hud";
    public static final String NAME = "visor.overlay.template."+ID+".name";
    public static final String DESCRIPTION = "visor.overlay.template."+ID+".description";

    private OverlayOptionsScreenRegion optionsScreenRegion;

    private RegionRenderTarget regionTarget;

    public VROverlayHUD(@NotNull VisorAddon owner,
                        @NotNull String id) {
        super(owner, id);
        setEnabled(true);
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void enableHUD(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.GUI_DISABLE_HUD) {
            if(isVisible()){
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void onRender(float partialTicks) {
        RenderTarget src = ClientContext.renderer.guiTarget.getTarget();
        updateRegionTargetFromSource(src);
    }

    @Override
    public void onPreTick() {
        RenderTarget src = ClientContext.renderer.guiTarget.getTarget();
        updateRegionTargetFromSource(src);
        super.onPreTick();
    }

    @Override
    public boolean updateVisibility() {
        return MC.screen == null
                && MC.player != null;
    }

    @Override
    public boolean supportsVisibilityUpdateOnRender() {
        return true;
    }


    private void updateRegionTargetFromSource(RenderTarget src) {
        if (src == null) {
            this.renderTarget = null;
            return;
        }

        // Read region from options; clamp additionally to the current source size for safety
        int guiW = src.width;
        int guiH = src.height;

        int rx = Math.max(0, Math.min(guiW, optionsScreenRegion.getRegionX()));
        int ryTopLeft = Math.max(0, Math.min(guiH, optionsScreenRegion.getRegionY()));
        int rw = Math.max(1, Math.min(guiW - rx, optionsScreenRegion.getRegionWidth()));
        int rh = Math.max(1, Math.min(guiH - ryTopLeft, optionsScreenRegion.getRegionHeight()));

        // Convert Y from top-left origin (GUI) to bottom-left origin (OpenGL framebuffer)
        int srcY0 = guiH - (ryTopLeft + rh);
        int srcY1 = guiH - ryTopLeft;
        int srcX0 = rx;
        int srcX1 = rx + rw;

        // Ensure valid bounds
        if (srcY0 < 0) srcY0 = 0;
        if (srcY1 > guiH) srcY1 = guiH;
        if (srcX0 < 0) srcX0 = 0;
        if (srcX1 > guiW) srcX1 = guiW;

        // Lazily create/resize the region target
        if (regionTarget == null) {
            regionTarget = new RegionRenderTarget(false);
            regionTarget.resize(rw, rh, true);
        } else if (regionTarget.width != rw || regionTarget.height != rh) {
            regionTarget.resize(rw, rh, true);
        }


        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, regionTarget.frameBufferId);

        // Copy color only, nearest filtering
        GlStateManager._glBlitFrameBuffer(
                srcX0, srcY0, srcX1, srcY1,
                0, 0, rw, rh,
                GL30.GL_COLOR_BUFFER_BIT,
                GL30.GL_NEAREST
        );

        // Unbind
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        // Use the cropped texture as the overlay render target
        this.renderTarget = regionTarget;
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createTemplateOptions() {
        optionsScreenRegion =  new OverlayOptionsScreenRegion(
                this,
                VisorAPI.client().getGuiManager().getGuiWidth(),
                VisorAPI.client().getGuiManager().getGuiHeight(),
                ()->ClientContext.renderer.guiTarget.getTarget(),
                (it)->{
                    it.setRegionX(0);
                    it.setRegionY(0);
                    it.setRegionWidth(it.getScreenWidth());
                    // Use the full screen height by default
                    it.setRegionHeight(it.getScreenHeight());
                }
        );
        return List.of(
                new OverlayOptionsPose(
                        this,
                        it->{
                            it.setTickPose(true);
                            it.setAimedRotation(false);
                            it.setPositionAnchor(PoseAnchor.HMD);
                            it.setPositionOffset(
                                    0,-0.1f, -1.2f
                            );
                            it.setRotationAnchor(PoseAnchor.HMD);
                            it.setRotationOffset(
                                    0,0,0
                            );
                            it.setScale(1.0f);
                        }
                ),
                optionsScreenRegion
        );
    }


    // Minimal concrete RenderTarget for region copies
    private static final class RegionRenderTarget extends RenderTarget {
        public RegionRenderTarget(boolean useDepth) {
            super(useDepth);
        }
    }
}
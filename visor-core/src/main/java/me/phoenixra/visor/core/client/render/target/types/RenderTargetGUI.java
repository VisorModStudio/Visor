package me.phoenixra.visor.core.client.render.target.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.render.target.RenderTargetHolder;
import me.phoenixra.visor.core.client.render.target.VRRenderTarget;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Getter
public class RenderTargetGUI implements RenderTargetHolder {
    private RenderTarget target = null;

    private final HashMap<VROverlayScreen, VRRenderTarget> overlayTargets = new HashMap<>();


    private int savedWidth;
    private int savedHeight;
    private boolean init;
    @Override
    public void init(int width, int height) throws Exception {
        target = new VRRenderTarget(
                "GUI",
                width, height,
                true,
                ()-> -1, true, false
        );
        GLUtils.checkGLError("GUI target setup");
        VisorClientImpl.LOGGER.info(target.toString());


        overlayTargets.clear();
        for(VROverlay overlay : ClientContext.overlayManager.getOverlaysRegistry().getSortedElements()) {
            if(overlay instanceof VROverlayScreen overlayScreen) {
                if(!overlay.isVisible() || !overlay.isEnabled()){
                    overlayTargets.put(overlayScreen, null);
                    overlayScreen.setRenderTarget(null);
                    continue;
                }
                VRRenderTarget renderTarget = new VRRenderTarget(
                        "Overlay " + overlayScreen.getId(),
                        width, height,
                        true,
                        () -> -1,
                        true, false
                );
                GLUtils.checkGLError("Overlay " + overlayScreen.getId() + " framebuffer setup");
                overlayTargets.put(overlayScreen, renderTarget);
                overlayScreen.setRenderTarget(renderTarget);
            }
        }

        savedWidth = width;
        savedHeight = height;
        init = true;
    }

    @Override
    public void resize(int width, int height) throws Exception {
        target.resize(
                width, height,
                Minecraft.ON_OSX
        );
        for(VRRenderTarget target : overlayTargets.values()) {
            if(target==null) continue;
            target.resize(
                    width, height,
                    Minecraft.ON_OSX
            );
        }
        if (MC.screen != null) {
            int screenWidth = MC.getWindow().getGuiScaledWidth();
            int screenHeight = MC.getWindow().getGuiScaledHeight();
            MC.screen.init(MC, screenWidth, screenHeight);
        }
        savedWidth = width;
        savedHeight = height;
    }

    @Override
    public void destroy() {
        if(target != null){
            target.destroyBuffers();
            target = null;
        }
        for(VRRenderTarget target : overlayTargets.values()) {
            if(target==null) continue;
            target.destroyBuffers();
        }
        overlayTargets.clear();

        init = false;
    }

    public void updateOverlayTarget(@NotNull VROverlayScreen overlayScreen){
        VRRenderTarget renderTarget = overlayTargets.get(overlayScreen);
        boolean visible = overlayScreen.isVisible();
        if(renderTarget == null && visible){
            renderTarget = new VRRenderTarget(
                    "Overlay " + overlayScreen.getId(),
                    savedWidth, savedHeight,
                    true,
                    () -> -1,
                    true, false
            );
            GLUtils.checkGLError("Overlay " + overlayScreen.getId() + " framebuffer setup");
            overlayTargets.put(overlayScreen, renderTarget);
        }else if(renderTarget != null && !visible){
            renderTarget.destroyBuffers();
            overlayTargets.put(overlayScreen, null);
        }
        overlayScreen.setRenderTarget(
                overlayTargets.get(overlayScreen)
        );
    }

}

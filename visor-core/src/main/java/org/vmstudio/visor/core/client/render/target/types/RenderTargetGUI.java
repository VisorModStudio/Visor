package org.vmstudio.visor.core.client.render.target.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.render.helpers.RenderStateHelper;
import org.vmstudio.visor.core.client.render.target.RenderTargetHolder;
import org.vmstudio.visor.core.client.render.target.VRRenderTarget;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

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
        for(VROverlay overlay : ClientContext.overlayManager.getOverlaysRegistry().getSortedComponents()) {
            if(overlay instanceof VROverlayScreen overlayScreen) {
                if(!overlay.isVisible() || !overlay.isEnabled()){
                    overlayTargets.put(overlayScreen, null);
                    overlayScreen.setRenderTarget(null);
                    continue;
                }
                VRRenderTarget renderTarget = new VRRenderTarget(
                        "Overlay " + overlayScreen.getId(),
                        overlayScreen.getRequestedWidth(),
                        overlayScreen.getRequestedHeight(),
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
        McRenderTarget.resize(target, width, height);
        for(var entry : overlayTargets.entrySet()) {
            if(entry.getValue()==null) continue;
            var overlay = entry.getKey();
            McRenderTarget.resize(
                    entry.getValue(),
                    overlay.getRequestedWidth(),
                    overlay.getRequestedHeight()
            );
            overlay.updateSize();
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
            // drain not related GL errors
            RenderStateHelper.drainExternalGLErrors("VROverlay target " + overlayScreen.getId());

            renderTarget = new VRRenderTarget(
                    "Overlay " + overlayScreen.getId(),
                    overlayScreen.getRequestedWidth(),
                    overlayScreen.getRequestedHeight(),
                    true,
                    () -> -1,
                    true, false
            );
            GLUtils.checkGLError("Overlay " + overlayScreen.getId() + " framebuffer setup");
            overlayTargets.put(overlayScreen, renderTarget);
        }else if(renderTarget != null && !visible){
            renderTarget.destroyBuffers();
            overlayTargets.put(overlayScreen, null);
        }else if(renderTarget != null){
            int neededWidth = overlayScreen.getRequestedWidth();
            int neededHeight = overlayScreen.getRequestedHeight();
            if(neededWidth != renderTarget.width
                    || neededHeight != renderTarget.height){
                renderTarget.destroyBuffers();
                McRenderTarget.resize(renderTarget, neededWidth, neededHeight);
                overlayScreen.updateSize();
            }
        }
        overlayScreen.setRenderTarget(
                overlayTargets.get(overlayScreen)
        );
    }

}

package org.vmstudio.visor.loader.forge.mixin;

import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class ForgeGameRendererMixin {

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false),
            method = "renderLevel")
    public void visor$keepVRAnglesInEyes(Camera camera, float yaw, float pitch) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            // eye passes must keep the VR pose angles
            camera.setAnglesInternal(yaw, pitch);
        }
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/client/event/ViewportEvent$ComputeCameraAngles;getRoll()F",
            remap = false),
            method = "renderLevel")
    public float visor$dropEventRollInEyes(ViewportEvent.ComputeCameraAngles event) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            return event.getRoll();
        }
        // VR supply roll in eye passes, so, we don't need it here
        return 0F;
    }
}

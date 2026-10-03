package org.vmstudio.visor.loader.forge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
//? if <1.21 {
/*import net.minecraftforge.client.event.ViewportEvent;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class ForgeGameRendererMixin {

    //? if >=26.3 {
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", remap = false),
            method = {"update", "extractCamera"}, require = 2)
    public void visor$keepVRAnglesInEyes(Camera camera, float yaw, float pitch, float roll, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            // eye passes must keep the VR pose angles
            original.call(camera, yaw, pitch, roll);
        }
    }
    //?} elif >=1.21 {
    /*@WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", remap = false),
            method = "renderLevel", require = 1)
    public void visor$keepVRAnglesInEyes(Camera camera, float yaw, float pitch, float roll, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            // eye passes must keep the VR pose angles
            original.call(camera, yaw, pitch, roll);
        }
    }
    *///?} else {
    /*@WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false),
            method = "renderLevel", require = 1)
    public void visor$keepVRAnglesInEyes(Camera camera, float yaw, float pitch, Operation<Void> original) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            // eye passes must keep the VR pose angles
            original.call(camera, yaw, pitch);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/client/event/ViewportEvent$ComputeCameraAngles;getRoll()F",
            remap = false),
            method = "renderLevel", require = 1)
    public float visor$dropEventRollInEyes(ViewportEvent.ComputeCameraAngles event, Operation<Float> original) {
        if (VRRenderState.getPhase().isVanilla()
                || !VRRenderState.getRenderPass().isEye()) {
            return original.call(event);
        }
        // VR supply roll in eye passes, so, we don't need it here
        return 0F;
    }
    *///?}
}

package org.vmstudio.visor.loader.forge.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.core.client.render.VRRenderState;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    /**
     * Forge 52 patches renderLevel to rebuild the camera rotation from the
     * ComputeCameraAngles euler angles: the forge-added
     * Camera.setRotation(yaw, pitch, roll) does rotationYXZ and rebuilds
     * the look/up/left vectors, and the world view matrix is then taken
     * from that quaternion. In VR that discards the exact tracked
     * orientation VRGameCamera.setupVR copied into camera.rotation()
     * (and drops headset roll), so the world view stops following the
     * HMD. Skip the euler rebuild in VR phases — same approach as the
     * 1.20.1 forge mixin that suppressed setAnglesInternal and the euler
     * mulPose calls.
     */
    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FFF)V",
            remap = false),
            method = "renderLevel")
    public void visor$keepVRCameraRotation(Camera camera, float yaw, float pitch, float roll) {
        if (VRRenderState.getPhase().isVanilla()) {
            camera.setRotation(yaw, pitch, roll);
        }
    }
}

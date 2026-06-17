package org.vmstudio.visor.compatibility.iris.mixin;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;

@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.uniforms.CameraUniforms",
        "net.coderbot.iris.uniforms.CameraUniforms"
}, remap = false)
public class IrisCameraUniformsMixin {
    @Inject(method = "getUnshiftedCameraPosition", at = @At("HEAD"), cancellable = true)
    private static void visor$useCurrentVrCameraPosition(CallbackInfoReturnable<Vector3d> cir) {
        if (VisorState.get().isNotActive() || VRRenderState.getPhase().isNotVRWorld()) {
            return;
        }

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == null || !renderPass.isWorld()) {
            return;
        }

        var renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var cameraPosition = RenderPoseHelper.getCameraPosition(renderPass, renderPose);
        cir.setReturnValue(new Vector3d(cameraPosition.x(), cameraPosition.y(), cameraPosition.z()));
    }
}
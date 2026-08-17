package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;


@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {

    @ModifyArg(
            method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;collectColumnInstances(Lnet/minecraft/world/level/Level;IFLnet/minecraft/world/phys/Vec3;ILjava/util/List;Ljava/util/List;)V"),
            index = 3
    )
    private Vec3 visor$rainAndSnowCentre(Vec3 cameraPosition) {
        if (VRRenderState.getRenderPass().isEye()) {
            var hmd = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition();
            return new Vec3(hmd.x(), hmd.y(), hmd.z());
        }
        return cameraPosition;
    }
}

// #!MC-VERSION:: 1.21.3+
package org.vmstudio.visor.mixin.client.renderer;

import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherMixin {

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "collectColumnInstances")
    public double visor$rainAndSnowX(double x) {
        return visor$hmdAxis(x, 0);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "collectColumnInstances")
    public double visor$rainAndSnowY(double y) {
        return visor$hmdAxis(y, 1);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "collectColumnInstances")
    public double visor$rainAndSnowZ(double z) {
        return visor$hmdAxis(z, 2);
    }

    @Unique
    private static double visor$hmdAxis(double vanilla, int axis) {
        if (!VRRenderState.getRenderPass().isEye()) {
            return vanilla;
        }
        Vector3fc hmd = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getHmd().getPosition();
        return axis == 0 ? hmd.x() : axis == 1 ? hmd.y() : hmd.z();
    }
}

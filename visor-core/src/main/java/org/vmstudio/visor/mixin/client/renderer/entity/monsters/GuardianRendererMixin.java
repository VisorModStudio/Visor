package org.vmstudio.visor.mixin.client.renderer.entity.monsters;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererMixin {

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    public void visor$vrRenderBeam(CallbackInfoReturnable<Vec3> cir,
                                   @Local(argsOnly = true) LivingEntity livingEntity) {
        if (VRRenderState.getPhase().isVanilla()
                || livingEntity != MC.getCameraEntity()) {
            return;
        }

        float worldScale = ClientContext
                .localPlayer
                .getPoseData(PlayerPoseType.TICK).getWorldScale();;
        Vector3f beamPos = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getHmd().getPosition()
                .sub(
                        0.0f,
                        0.3f * worldScale,
                        0.0f,
                        new Vector3f()
                );;
        cir.setReturnValue(new Vec3(beamPos));
    }
}

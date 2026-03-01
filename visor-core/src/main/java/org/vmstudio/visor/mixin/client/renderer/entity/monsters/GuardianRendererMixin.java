package org.vmstudio.visor.mixin.client.renderer.entity.monsters;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererMixin {

    @Shadow
    protected abstract Vec3 getPosition(LivingEntity livingEntity, double d, float f);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/GuardianRenderer;getPosition(Lnet/minecraft/world/entity/LivingEntity;DF)Lnet/minecraft/world/phys/Vec3;"), method = "render(Lnet/minecraft/world/entity/monster/Guardian;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public Vec3 visor$vrRenderBeam(GuardianRenderer instance,
                               LivingEntity livingEntity,
                               double yOffset,
                               float partialTick) {
        if (VRRenderState.getPhase().isVanilla()
                || livingEntity != MC.getCameraEntity()) {
            return this.getPosition(livingEntity, yOffset, partialTick);
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
        return new Vec3(beamPos);
    }
}

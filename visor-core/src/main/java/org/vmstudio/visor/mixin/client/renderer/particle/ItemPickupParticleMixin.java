package org.vmstudio.visor.mixin.client.renderer.particle;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleMixin {

    @Final
    @Shadow
    private Entity target;
    @Final
    @Shadow
    private Entity itemEntity;

    @Unique
    private Vector3fc visor$playerPos;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0), method = "render")
    public double visor$vrPosX(double partialTick,
                              double oldValue,
                              double newValue) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            visor$playerPos = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition();
            oldValue = newValue = visor$playerPos.x();
        }

        return Mth.lerp(partialTick, oldValue, newValue);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = "render")
    public double visor$vrPosY(double partialTick,
                              double oldValue,
                              double newValue) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            float offset = 0.5F + itemEntity.getBbHeight();
            oldValue = newValue = visor$playerPos.y() - offset;
        }

        return Mth.lerp(partialTick, oldValue, newValue);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = "render")
    public double visor$vrPosZ(double partialTick,
                              double oldValue,
                              double newValue) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            oldValue = newValue = visor$playerPos.z();
            visor$playerPos = null;
        }

        return Mth.lerp(partialTick, oldValue, newValue);
    }
}

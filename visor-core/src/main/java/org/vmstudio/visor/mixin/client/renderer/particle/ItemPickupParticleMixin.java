package org.vmstudio.visor.mixin.client.renderer.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleMixin {

    @Final
    @Shadow
    private Entity target;
    //? if <1.21.9 {
    /*@Final
    @Shadow
    private Entity itemEntity;
    *///?}

    @Unique
    private Vector3fc visor$playerPos;

    //? if >=1.21.4 {
    private static final String RENDER = "renderCustom";
    //?} else {
    /*private static final String RENDER = "render";
    *///?}

    // 1.21.9 extracts the pickup particle instead of rendering it, the three lerps moved out of this class:
    // the VR re-anchor to the headset has no hook here any more and is disabled until it is redesigned.

    //? if <1.21.9 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0), method = RENDER)
    public double visor$vrPosX(double partialTick,
                              double oldValue,
                              double newValue, Operation<Double> original) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            visor$playerPos = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition();
            oldValue = newValue = visor$playerPos.x();
        }

        return original.call(partialTick, oldValue, newValue);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = RENDER)
    public double visor$vrPosY(double partialTick,
                              double oldValue,
                              double newValue, Operation<Double> original) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            float offset = 0.5F + itemEntity.getBbHeight();
            oldValue = newValue = visor$playerPos.y() - offset;
        }

        return original.call(partialTick, oldValue, newValue);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = RENDER)
    public double visor$vrPosZ(double partialTick,
                              double oldValue,
                              double newValue, Operation<Double> original) {
        if (VisorState.get().isActive()
                && target == MC.player) {
            oldValue = newValue = visor$playerPos.z();
            visor$playerPos = null;
        }

        return original.call(partialTick, oldValue, newValue);
    }
    *///?}
}

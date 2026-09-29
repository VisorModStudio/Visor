package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownTrident.class)
public class ThrownTridentMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition()Lnet/minecraft/world/phys/Vec3;"), method = "tick()V")
    public Vec3 visor$tick(Entity entity, Operation<Vec3> original) {
        Vec3 eyePosition = original.call(entity);
        if (!(entity instanceof ServerPlayer player)) {
            return eyePosition;
        }
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVRPlayer(player);
        if (vrPlayer == null) {
            return eyePosition;
        }

        return vrPlayer.getPoseData()
                .getActiveHand()
                .getPositionVec3();
    }
}

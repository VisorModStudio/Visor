// #!MC-VERSION:: 1.20.1-1.20.4
package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    public Vec3 visor$onDeflectByVRPlayer(Entity instance, Operation<Vec3> original) {
        if (!(instance instanceof ServerPlayer player)) {
            return original.call(instance);
        }
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVRPlayer(player);
        if (vrPlayer == null) {
            return original.call(instance);
        }

        return vrPlayer.getPoseData()
                .getHmd()
                .getDirectionVec3();
    }
}

package me.phoenixra.visor.core.mixin.common.world.entity.projectiles;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    public Vec3 visor$onDeflectByVRPlayer(Entity instance) {
        if (!(instance instanceof ServerPlayer player)) {
            return instance.getLookAngle();
        }
        VRServerPlayer serverPlayer = VisorAPI.server()
                .getVrPlayer(player);
        if (serverPlayer == null || !serverPlayer.isVr()) {
            return instance.getLookAngle();
        }

        return serverPlayer.getHmdDir();
    }
}

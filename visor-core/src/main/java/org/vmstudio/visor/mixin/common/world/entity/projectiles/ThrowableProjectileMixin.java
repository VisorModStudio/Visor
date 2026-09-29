// #!MC-VERSION:: 1.21.3+
package org.vmstudio.visor.mixin.common.world.entity.projectiles;


import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;

// 1.21.2 moved the thrower constructor down to ThrowableItemProjectile
@Mixin(ThrowableItemProjectile.class)
public abstract class ThrowableProjectileMixin extends Entity {

    protected ThrowableProjectileMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V")
    public void visor$initVrPos(EntityType<? extends ThrowableItemProjectile> entityType,
                               LivingEntity entity,
                               Level level,
                               ItemStack itemStack,
                               CallbackInfo info) {
        visor$moveToHand(entity);
    }

    @Unique
    private void visor$moveToHand(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVRPlayer(player);
        if (vrPlayer == null) {
            return;
        }
        var activeHand = vrPlayer.getPoseData().getActiveHand();

        Vec3 handPos = activeHand.getPositionVec3();
        Vec3 handDir = activeHand.getDirectionVec3()
                .scale(0.6F);
        this.setPos(
                handPos.x + handDir.x,
                handPos.y + handDir.y,
                handPos.z + handDir.z
        );
    }
}

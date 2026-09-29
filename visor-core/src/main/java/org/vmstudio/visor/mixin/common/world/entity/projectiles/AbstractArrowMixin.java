package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity {

    protected AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    private double baseDamage;

    // 1.20.3 gave every arrow constructor the pickup ItemStack,
    // 1.21 the firing weapon as well
    //? if >=1.21 {
    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V")
    //?} elif >=1.20.3 {
    /*@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)V")
    *///?} else {
    /*@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V")
    *///?}
    public void visor$setupPos(CallbackInfo ci, @Local(argsOnly = true) LivingEntity shooter) {
        if (!(shooter instanceof ServerPlayer player)) {
           return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVRPlayer(player);
        if (vrPlayer == null) {
            return;
        }

        var activeHand = vrPlayer.getPoseData()
                .getActiveHand();
        Vec3 handPos = activeHand.getPositionVec3();
        Vec3 handDir = activeHand.getDirectionVec3();

        this.setPos(
                handPos.x + handDir.x,
                handPos.y + handDir.y,
                handPos.z + handDir.z
        );
    }
}

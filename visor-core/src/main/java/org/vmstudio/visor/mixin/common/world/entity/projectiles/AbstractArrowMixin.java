package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
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

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V")
    public void visor$setupPos(EntityType<? extends AbstractArrow> entityType, LivingEntity shooter, Level level, CallbackInfo ci
    ) {
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

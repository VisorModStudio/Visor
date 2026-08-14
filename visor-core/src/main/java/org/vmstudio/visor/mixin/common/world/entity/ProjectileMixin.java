package org.vmstudio.visor.mixin.common.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements TraceableEntity {

    @Unique
    private Vec3 visor$savedHandDir;

    public ProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }


    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"), ordinal = 3, argsOnly = true)
    public float visor$pVelocity(float pVelocity, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return pVelocity;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
        if (vrPlayer == null) {
            return pVelocity;
        }
        var poseData = vrPlayer.getPoseData();
        this.visor$savedHandDir = poseData.getActiveHand()
                .getDirectionVec3();

        return pVelocity;
    }

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public float visor$vrPitch(float pX, Entity pProjectile) {
        if (this.visor$savedHandDir != null) {
            return -((float) Math.toDegrees(
                    Math.asin(this.visor$savedHandDir.y / this.visor$savedHandDir.length()))
            );
        }
        return pX;
    }

    @ModifyVariable(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"), ordinal = 1, argsOnly = true)
    public float visor$vrYaw(float pY, Entity pProjectile) {
        if (this.visor$savedHandDir != null) {
            float toRet = (float) Math.toDegrees(
                    Mth.atan2(
                            -this.visor$savedHandDir.x,
                            this.visor$savedHandDir.z
                    )
            );
            this.visor$savedHandDir = null;
            return toRet;
        }
        return pY;
    }

    @Unique
    private boolean visor$isBow(ItemStack itemStack) {
        return itemStack.getItem() instanceof BowItem;
    }

    /**
     * 1.21.1: fireball punch-deflection moved from AbstractHurtingProjectile#hurt
     * into the ProjectileDeflection lambdas. AIM_DEFLECT aims along the
     * deflector's look angle; for VR players that has to be the HMD direction.
     * The lambda's synthetic name differs between loaders (intermediary vs
     * javac names), so the interface call inside Projectile#deflect is wrapped
     * instead of the lambda body.
     */
    @WrapOperation(method = "deflect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/ProjectileDeflection;deflect(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/util/RandomSource;)V"))
    private void visor$deflectAlongHmd(ProjectileDeflection instance, Projectile projectile, Entity entity,
                                       RandomSource random, Operation<Void> original) {
        original.call(instance, projectile, entity, random);
        if (instance == ProjectileDeflection.AIM_DEFLECT
                && entity instanceof ServerPlayer player) {
            VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
            if (vrPlayer != null) {
                projectile.setDeltaMovement(
                        vrPlayer.getPoseData().getHmd().getDirectionVec3().normalize()
                );
            }
        }
    }
}

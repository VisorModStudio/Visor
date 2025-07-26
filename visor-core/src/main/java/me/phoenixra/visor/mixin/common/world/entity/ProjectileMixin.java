package me.phoenixra.visor.mixin.common.world.entity;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
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
        VRServerPlayer serverPlayer = VisorAPI.server().getVrPlayer(player);
        if (serverPlayer == null || !serverPlayer.isVr()) {
            return pVelocity;
        }
        this.visor$savedHandDir = serverPlayer.getActiveHandDir();

        Projectile instance = ((Projectile) (Object) this);

        boolean isArrow = (instance instanceof AbstractArrow)
                && !(instance instanceof ThrownTrident);
        if (!isArrow || serverPlayer.getBowTension() <= 0) {
            return pVelocity;
        }
        boolean bowInMainHand = visor$isBow(
                player.getItemInHand(InteractionHand.MAIN_HAND)
        );
        if (bowInMainHand) {
            this.visor$savedHandDir = serverPlayer
                    .getControllerPos(ControllerHand.MAIN)
                    .subtract(
                            serverPlayer.getControllerPos(
                                    ControllerHand.OFFHAND
                            )
                    ).normalize();
        } else {
            this.visor$savedHandDir = serverPlayer
                    .getControllerPos(ControllerHand.OFFHAND)
                    .subtract(
                            serverPlayer.getControllerPos(
                                    ControllerHand.MAIN
                            )
                    ).normalize();
        }

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
}

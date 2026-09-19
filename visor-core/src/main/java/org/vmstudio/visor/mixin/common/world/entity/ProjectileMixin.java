// #!MC-VERSION:: 1.20.6+
package org.vmstudio.visor.mixin.common.world.entity;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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


    @Inject(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"))
    public void visor$vrSpawnPos(Entity shooter, float pX, float pY, float pZ,
                                 float pVelocity, float pInaccuracy, CallbackInfo ci) {
        if (!((Object) this instanceof AbstractWindCharge)
                || !(shooter instanceof ServerPlayer player)) {
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
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
}

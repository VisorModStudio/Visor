package org.vmstudio.visor.mixin.common.world.entity.projectiles;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.CommonUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    public void visor$setupPos(EntityType entityType, LivingEntity livingEntity, Level level, CallbackInfo ci
    ) {
        if (!(livingEntity instanceof ServerPlayer player)) {
           return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVrPlayer(player);
        if (vrPlayer == null) {
            return;
        }

        var activeHand = vrPlayer.getPose()
                .getActiveHand();
        Vec3 handPos = activeHand.getPositionVec3();
        Vec3 handDir = activeHand.getDirectionVec3();

        this.setPos(
                handPos.x + handDir.x,
                handPos.y + handDir.y,
                handPos.z + handDir.z
        );
    }

    @Inject(at = @At("HEAD"), method = "onHitEntity")
    public void visor$damageMultiplier(EntityHitResult entityHitResult,
                                      CallbackInfo ci
    ) {
        if (!(((Projectile) (Object) this).getOwner() instanceof ServerPlayer owner)){
            return;
        }
        VRServerPlayer serverPlayer = VisorAPI.server()
                .getVrPlayer(owner);
        if (serverPlayer == null){
            return;
        }
        Vec3 hitPosHead = visor$getHitPosIfHead(entityHitResult);

        if(hitPosHead == null){
            baseDamage *= 2;
            return;
        }

        //particles
        ((ServerLevel) this.level()).sendParticles(
                owner,
                ParticleTypes.CRIT,
                true, // always render the hit particles on the client
                hitPosHead.x,
                hitPosHead.y,
                hitPosHead.z,
                5,
                -this.getDeltaMovement().x,
                -this.getDeltaMovement().y,
                -this.getDeltaMovement().z,
                0.1
        );
        //sound
        owner.connection.send(
                new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
                                SoundEvents.ITEM_BREAK
                        ),
                        SoundSource.PLAYERS,
                        owner.getX(),
                        owner.getY(),
                        owner.getZ(),
                        0.7f, 0.5f,
                        owner.level().random.nextLong()
                )
        );

        baseDamage *= 3;
    }

    @Unique
    private Vec3 visor$getHitPosIfHead(EntityHitResult hit) {
        AABB headBox = CommonUtils.getEntityHeadHitBox(hit.getEntity(), 0.3);
        if(headBox == null){
            return null;
        }
        Vec3 originHitPos = hit.getEntity()
                .getBoundingBox()
                .clip(
                        this.position(),
                        this.position()
                                .add(
                                        this.getDeltaMovement()
                                                .scale(2.0)
                                )
                ).orElse(
                        this.position().add(this.getDeltaMovement())
                );
        return headBox
                .clip(this.position(), originHitPos)
                .orElse(headBox.contains(this.position())
                        ? this.position()
                        : null
                );
    }
}

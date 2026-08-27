package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.compatibility.ItemClassifier;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
        extends Common_PlayerMixin implements ServerPlayerExtension {

    @Shadow
    @Final
    public MinecraftServer server;

    @Unique
    private float visor$rotationYCached;

    @Unique
    private int visor$offhandSlotCached;

    @Unique
    private ItemStack visor$poseBlockItem;

    @Unique
    private InteractionHand visor$poseBlockHand;




    /* *************** *\
  //--------DATA--------\\
    \* **************** */


    @WrapMethod(method = "readAdditionalSaveData")
    protected void visor$wrapReadData(CompoundTag compound, Operation<Void> original) {
        original.call(compound);
        visor$rotationYCached = compound.getFloat("visor$rotation_y");
        if(compound.contains("visor$offhand_slot")){
            visor$offhandSlotCached = compound.getInt("visor$offhand_slot");
        }else{
            visor$offhandSlotCached = -1;
        }
    }
    @WrapMethod(method = "addAdditionalSaveData")
    protected void visor$wrapSaveData(CompoundTag compound, Operation<Void> original) {
        original.call(compound);
        compound.putFloat("visor$rotation_y", visor$rotationYCached);
        compound.putFloat("visor$offhand_slot", visor$offhandSlotCached);
    }

    /* *************** *\
  //--------OTHER--------\\
    \* **************** */

    @Inject(at = @At("HEAD"), method = "setCamera", cancellable = true)
    private void visor$noSpectateEntityInVR(Entity entityToSpectate,
                                        CallbackInfo ci){
        VRServerPlayer vrPlayer = visor$getVrPlayer();
        if(vrPlayer != null){
            ci.cancel();
        }
    }


    @Override
    protected void visor$injectSetPosRaw(double x, double y, double z, CallbackInfo ci) {
        VRServerPlayer vrPlayer = visor$getVrPlayer();
        if(vrPlayer != null){
            vrPlayer.getPoseData().resetOrigin(
                    visor$getPlayer().position().toVector3f()
            );
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;tick()V", shift = Shift.AFTER), method = "doTick()V")
    public void visor$tickBodyPose(CallbackInfo info) {
        VRServerPlayer vrPlayer = visor$getVrPlayer();

        if (vrPlayer != null
                && vrPlayer.isCrawling()) {
            visor$getPlayer().setPose(Pose.SWIMMING);
        }
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = Shift.BEFORE), method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void visor$vrItemDrop(ItemStack itemStack,
                                boolean dropAround,
                                boolean includeName,
                                CallbackInfoReturnable<ItemEntity> info,
                                ItemEntity itemEntity) {
        VRServerPlayer vrPlayer = visor$getVrPlayer();
        if (vrPlayer == null
                || dropAround) {
            return;
        }

        var mainHand = vrPlayer.getPoseData().getMainHand();
        var handDir = mainHand.getDirection()
                .mul(0.3F, new Vector3f());
        var handPos = mainHand.getPosition();
        itemEntity.setDeltaMovement(
                handDir.x,
                handDir.y,
                handDir.z
        );
        itemEntity.setPos(
                handPos.x() + itemEntity.getDeltaMovement().x(),
                handPos.y() + itemEntity.getDeltaMovement().y(),
                handPos.z() + itemEntity.getDeltaMovement().z()
        );
    }

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    public void visor$canGetHurtByPlayer(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = damageSource.getEntity();
        ServerPlayer damager = null;

        if (entity instanceof ServerPlayer) {
            damager = (ServerPlayer) entity;
        } else if ((entity instanceof AbstractArrow && (((AbstractArrow) entity).getOwner() instanceof ServerPlayer))) {
            damager = (ServerPlayer) ((AbstractArrow) entity).getOwner();
        }
        if(damager == null){
            return;
        }

        VRServerPlayer damagerPlayer = VisorAPI.server().getVRPlayer(damager);
        VRServerPlayer thisPlayer = visor$getVrPlayer();
        boolean victimHasVR;
        boolean damagerHasVR;

        damagerHasVR = damagerPlayer != null;
        victimHasVR = thisPlayer != null;

        boolean blockedDamage = false;
        String blockedDamageCase = "";

        if (!VRServerSettings.isPvpVRvsVR()
                && damagerHasVR && victimHasVR) {
            blockedDamage = true;
            blockedDamageCase = "Server: cancelled VR vs VR player damage";

        } else if (!VRServerSettings.isPvpVRvsVanilla()
                && ((!damagerHasVR && victimHasVR) || (damagerHasVR && !victimHasVR))) {
            blockedDamage = true;
            blockedDamageCase = "Server: cancelled NonVR vs VR player damage";
        }

        if(!blockedDamage) return;

        if(VRServerSettings.isNotifyPvpBlocked()) {
            damager.sendSystemMessage(Component.literal(blockedDamageCase));
        }
        cir.setReturnValue(false);
    }

    @Override
    protected void visor$wrapSweepAttack(Operation<Void> original) {
        VRServerPlayer vrPlayer = visor$getVrPlayer();

        if (vrPlayer != null) {
            var handPose = vrPlayer.getPoseData().getHand(
                    visor$attackHand(vrPlayer)
            );

            var handDir = handPose.getDirection();
            var handPos = handPose.getPosition();

            float handAngle = (float) Mth.atan2(-handDir.x(), handDir.z());
            double offsetX = -Mth.sin(handAngle);
            double offsetZ = Mth.cos(handAngle);

            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        handPos.x() + offsetX,
                        handPos.y(),
                        handPos.z() + offsetZ,
                        0,
                        offsetX, 0.0D, offsetZ,
                        0.0D
                );
            }
        } else {
            original.call();
        }
    }



    @Override
    protected boolean visor$checkPoseBlocking(boolean isBlocking,
                                                    DamageSource damageSource,
                                                    LocalBooleanRef poseBlocked) {
        visor$poseBlockItem = null;
        visor$poseBlockHand = null;

        if (isBlocking || !VRServerSettings.isRoomscaleShieldBlocking()) {
            return isBlocking;
        }

        Vec3 dmgPos = damageSource.getSourcePosition();
        if (dmgPos == null) {
            return false;
        }
        if (damageSource.getDirectEntity() instanceof AbstractArrow arrow
                && arrow.getPierceLevel() > 0) {
            return false;
        }

        VRServerPlayer vrPlayer = visor$getVrPlayer();
        if (vrPlayer == null || !vrPlayer.hasPoseData()) {
            return false;
        }

        ServerPlayer player = visor$getPlayer();
        boolean projectile = false;
        Entity direct = damageSource.getDirectEntity();
        if (direct != null && dmgPos == direct.position()) {
            projectile = direct instanceof Projectile;
            dmgPos = direct.getBoundingBox().getCenter()
                    .subtract(direct.getDeltaMovement().normalize());
        }

        for (HandType hand : HandType.values()) {
            ItemStack stack = player.getItemBySlot(
                    hand == HandType.MAIN
                            ? EquipmentSlot.MAINHAND
                            : EquipmentSlot.OFFHAND
            );
            if (!visor$isShield(stack)
                    || player.getCooldowns().isOnCooldown(stack.getItem())) {
                continue;
            }

            Vector3fc sideDir;
            if (vrPlayer.isLeftHanded()) {
                sideDir = hand == HandType.MAIN
                        ? VRMathUtils.LEFT_VECTOR : VRMathUtils.RIGHT_VECTOR;
            } else {
                sideDir = hand == HandType.MAIN
                        ? VRMathUtils.RIGHT_VECTOR : VRMathUtils.LEFT_VECTOR;
            }

            var handPose = vrPlayer.getPoseData().getHand(hand);
            Vec3 shieldDir = handPose.getCustomVector3(sideDir);

            // threshold is cos(60 deg): the shield face covers a 120-degree arc
            double angle;
            if (projectile) {
                Vec3 dmgDir = dmgPos.subtract(handPose.getPositionVec3()).normalize();
                angle = shieldDir.dot(dmgDir);
            } else {
                Vec3 dmgDir = dmgPos.subtract(player.position());
                dmgDir = new Vec3(dmgDir.x, 0, dmgDir.z).normalize();
                Vec3 shieldHor = new Vec3(shieldDir.x, 0, shieldDir.z).normalize();
                angle = shieldHor.dot(dmgDir);
            }

            if (angle > 0.5) {
                poseBlocked.set(true);
                visor$poseBlockItem = stack;
                visor$poseBlockHand = hand.asInteractionHand();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void visor$poseBlockShieldDamage(float damageAmount, Operation<Void> original) {
        if (visor$poseBlockItem == null) {
            original.call(damageAmount);
            return;
        }
        ItemStack backup = this.useItem;
        this.useItem = visor$poseBlockItem;
        try {
            original.call(damageAmount);
        } finally {
            this.useItem = backup;
            visor$poseBlockItem = null;
            visor$poseBlockHand = null;
        }
    }

    @Override
    protected InteractionHand visor$poseBlockShieldHand(InteractionHand original) {
        return visor$poseBlockHand != null ? visor$poseBlockHand : original;
    }

    @Unique
    private boolean visor$isShield(ItemStack stack) {
        return !stack.isEmpty()
                && (ItemClassifier.SHIELD.is(stack)
                || stack.getUseAnimation() == UseAnim.BLOCK);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void visor$noAttackWhileShieldUp(Entity target, CallbackInfo ci) {
        if (VRServerSettings.isAttacksWhileBlocking()) {
            return;
        }
        if (visor$getVrPlayer() != null && visor$getPlayer().isBlocking()) {
            ci.cancel();
        }
    }

    @Override
    protected void visor$spawnVRItemParticles(ItemStack itemStack,
                                              int count,
                                              CallbackInfo ci){
        LivingEntity instance = (LivingEntity) (Object)this;
        if(!(instance instanceof ServerPlayer player)){
            return;
        }
        ci.cancel();

        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
        for (int i = 0; i < count; ++i) {
            Vec3 velocity = new Vec3(
                    ((double) this.random.nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D,
                    0.0D
            );
            velocity = velocity.xRot(
                    -this.getXRot() * ((float) Math.PI / 180F)
            );
            velocity = velocity.yRot(
                    -this.getYRot() * ((float) Math.PI / 180F)
            );
            double verticalOffset = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3 particlePos;
            if (vrPlayer != null) {
                InteractionHand interactionhand = player.getUsedItemHand();

                if (interactionhand == InteractionHand.MAIN_HAND) {
                    particlePos = vrPlayer.getPoseData()
                            .getMainHand()
                            .getPositionVec3();
                } else {
                    particlePos = vrPlayer.getPoseData()
                            .getOffhand()
                            .getPositionVec3();
                }
            }else{
                particlePos = new Vec3(
                        ((double) this.random.nextFloat() - 0.5D) * 0.3D,
                        verticalOffset,
                        0.6D
                );
                particlePos = particlePos.xRot(
                        -this.getXRot() * ((float) Math.PI / 180F)
                );
                particlePos = particlePos.yRot(
                        -this.getYRot() * ((float) Math.PI / 180F)
                );
                particlePos = particlePos.add(
                        this.getX(),
                        this.getEyeY(),
                        this.getZ()
                );
            }
            //to not have an annoying particles displaying
            //too close to player eyes
            particlePos = particlePos
                    .add(0,-0.8,0);
            if (this.level() instanceof ServerLevel) {
                ((ServerLevel)this.level()).sendParticles(
                        new ItemParticleOption(
                                ParticleTypes.ITEM,
                                itemStack
                        ),
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        1, velocity.x,
                        velocity.y + 0.05D,
                        velocity.z,
                        0.0
                );
            }else {
                this.level().addParticle(
                        new ItemParticleOption(
                                ParticleTypes.ITEM,
                                itemStack
                        ),
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        velocity.x,
                        velocity.y + 0.05D,
                        velocity.z
                );
            }
        }
    }

    @Unique
    private VRServerPlayer visor$getVrPlayer() {
        return VisorAPI.server().getVRPlayer((ServerPlayer) (Object) this);
    }

    @Unique
    private ServerPlayer visor$getPlayer() {
        return (ServerPlayer) (Object) this;
    }

    @Unique
    public void visor$setRotationYCached(float visor$rotationY) {
        this.visor$rotationYCached = visor$rotationY;
    }
    @Unique
    public float visor$getRotationYCached() {
        return visor$rotationYCached;
    }

    @Unique
    @Override
    public void visor$setOffhandSlotCached(int slot) {
        this.visor$offhandSlotCached = slot;
    }
    @Unique
    @Override
    public int visor$getOffhandSlotCached() {
        return visor$offhandSlotCached;
    }

}

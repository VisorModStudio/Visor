package org.vmstudio.visor.mixin.client.player;

import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

import net.minecraft.util.Mth;
import org.vmstudio.visor.api.client.input.HapticFeedback;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=1.21.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//?}
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.toserver.TeleportMovePayloadToServer;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.tasks.types.movement.vehicle.TaskVehicle;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.mixin.common.player.Common_PlayerMixin;
import org.vmstudio.visor.extensions.client.entity.LocalPlayerExtension;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.11 {
import net.minecraft.world.phys.HitResult;
import org.vmstudio.visor.core.client.player.VRAimPicker;
//?}


@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Common_PlayerMixin implements LocalPlayerExtension {


    @Final
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    private boolean startedUsingItem;
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    private InteractionHand usingItemHand;

    @Unique
    private Vec3 visor$stuckSpeedMul = Vec3.ZERO;
    @Unique
    private boolean visor$stepUpRaised = false;

    @Unique
    private boolean visor$teleported;

    @Unique
    private double visor$roomYOffsetApplied;


    @Shadow
    protected abstract void updateAutoJump(float f, float g);

    //? if <26.3 {
    /*@Shadow
    public abstract void swing(InteractionHand interactionHand);
    *///?}



    /* ****************** *\
      //--------VEHICLE--------\\
        \* ****************** */
    @Inject(at = @At("TAIL"), method = "startRiding")
    //? if >=1.21.9 {
    public void visor$onStartRiding(Entity vehicle, boolean bl, boolean bl2, CallbackInfoReturnable<Boolean> cir) {
    //?} else {
    /*public void visor$onStartRiding(Entity vehicle, boolean bl, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        TaskVehicle.getInstance()
                .onStartRiding(
                        vehicle
                );

    }

    @Inject(at = @At("TAIL"), method = "removeVehicle")
    public void visor$onStopRiding(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        TaskVehicle.getInstance()
                .onStopRiding();
    }



     /* ****************** *\
   //--------MOVEMENT--------\\
     \* ****************** */

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.BEFORE), method = "tick")
    public void visor$preTick(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        visor$tickRoomYOffset();
        ClientContext.localPlayer.updatePlayerLook(
                (LocalPlayer) (Object) this,
                PlayerPoseType.TICK
        );
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), method = "tick")
    public void visor$postTick(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        var player = visor$getPlayer();
        if (ClientContext.localPlayer.isCrawling()) {
            player.setPose(Pose.SWIMMING);
        }
        ClientContext.localPlayer.updatePlayerLook(
                player,
                PlayerPoseType.TICK
        );
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"), method = "aiStep")
    public void visor$tickPlayer(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        ClientContext.localPlayer.tickPlayer(
                visor$getPlayer()
        );
    }



    @Override
    protected void visor$wrapMove(MoverType type, Vec3 pos, Operation<Void> original) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)
                || Minecraft.getInstance().getCameraEntity() != visor$getPlayer()) {
            visor$releaseWalkUp();
            original.call(type, pos);
            return;
        }
        this.visor$stuckSpeedMul = this.stuckSpeedMultiplier;

        if (pos.length() == 0 || this.isPassenger()) {
            original.call(type, pos);
            return;
        }

        Vector3fc origin = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getOrigin();

        if (!visor$isDrivenExternally()) {
            // roomscale walking. only y is minecraft driven
            original.call(type, new Vec3(0.0D, pos.y, 0.0D));
            ClientContext.localPlayer.setOrigin(
                    origin.x(),
                    visor$originY(),
                    origin.z(),
                    false
            );
            return;
        }

        double xOffset = origin.x() - this.getX();
        double zOffset = origin.z() - this.getZ();
        double prevX = this.getX();
        double prevZ = this.getZ();

        if (VRClientSettings.isWalkUpEnabled()
                && this.visor$stepUpRaised
                && visor$isApproachingInteractable(pos)) {
            visor$releaseWalkUp();
        }

        original.call(type, pos);

        if (VRClientSettings.isWalkUpEnabled()) {
            boolean smartBlocked = visor$isApproachingInteractable(this.getDeltaMovement());
            this.visor$stepUpRaised = this.getBlockJumpFactor() == 1.0F
                    && !smartBlocked;
            McVersionUtils.setStepHeight(
                    (LocalPlayer) (Object) this,
                    this.visor$stepUpRaised
                            ? 1.0F : 0.6F
            );
        } else {
            visor$releaseWalkUp();
            this.updateAutoJump(
                    (float) (this.getX() - prevX),
                    (float) (this.getZ() - prevZ)
            );
        }

        ClientContext.localPlayer.setOrigin(
                (float) (this.getX() + xOffset),
                visor$originY(),
                (float) (this.getZ() + zOffset),
                false
        );
    }

    @Unique
    private float visor$originY() {
        return (float) (this.getY()
                + ClientContext.localPlayer.getCameraOffsetWorld()
                + this.visor$getRoomYOffset());
    }

    @Unique
    private void visor$releaseWalkUp() {
        if (this.visor$stepUpRaised) {
            // 0.6F is from LivingEntity's constructor
            McVersionUtils.setStepHeight((LocalPlayer) (Object) this, 0.6F);
            this.visor$stepUpRaised = false;
        }
    }

    @Unique
    private boolean visor$isDrivenExternally() {
        final double minDriftSpeed = 0.0095D;
        return this.zza != 0.0F
                || this.isFallFlying()
                || Math.abs(this.getDeltaMovement().x) > minDriftSpeed
                || Math.abs(this.getDeltaMovement().z) > minDriftSpeed;
    }

    @Unique
    private boolean visor$isApproachingInteractable(Vec3 motion) {
        var player = visor$getPlayer();
        return CommonUtils.hasInteractableBlockAhead(
                player.level(),
                player.getBoundingBox(),
                motion,
                0.4D
        );
    }
    @Override
    protected void visor$wrapMoveRelative(float amount, Vec3 relative, Operation<Void> original){
        if (VisorState.get().isNotActive() || !visor$isThisPlayerLocal(this)) {
            original.call(amount, relative);
            return;
        }

        final double minInputLengthSq = 0.0005D;

        Vec3 horizontal = new Vec3(relative.x, 0.0D, relative.z);
        double lengthSq = horizontal.lengthSqr();
        if (lengthSq < minInputLengthSq) {
            return;
        }
        // same as vanilla Entity.getInputVector
        Vec3 move = (lengthSq > 1.0D ? horizontal.normalize() : horizontal).scale(amount);


        var rotationElement = ClientContext.localPlayer.getRotationElement(PlayerPoseType.TICK);
        if (this.isSwimming()) {
            rotationElement = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getHmd();
        }

        //SWIMMING OR FLYING
        if (!this.isPassenger() && (this.isSwimming() || this.getAbilities().flying)) {
            move = move.xRot(rotationElement.getPitch());
        }
        move = move.yRot(rotationElement.getYaw() * -1);


        float yFactor = this.getAbilities().flying
                ? 5f
                : 1f;

        this.setDeltaMovement(
                this.getDeltaMovement().x + move.x,
                this.getDeltaMovement().y + move.y * (double) yFactor,
                this.getDeltaMovement().z + move.z
        );
    }

    @Override
    protected void visor$injectSetPos(double x, double y, double z, CallbackInfo ci) {
        boolean shouldReset = (x + y + z) == 0;
        var thisPlayer = ((LocalPlayer) (Object) this);

        if (!shouldReset
                && thisPlayer.xOld == x
                && thisPlayer.yOld == y
                && thisPlayer.zOld == z) {
            shouldReset = true;
        }

        if (this.isPassenger()) {
            Vec3 premountPos = TaskVehicle.getInstance().premountPosRoom;
            premountPos = premountPos
                    .yRot(
                            ClientContext.localPlayer
                                    .getPoseData(PlayerPoseType.PREV_TICK)
                                    .getRotationY()
                    );
            x = x - premountPos.x;
            z = z - premountPos.z;
            y += ClientContext.localPlayer.getCameraOffsetWorld();
            ClientContext.localPlayer.setOrigin((float) x, (float) y, (float) z, shouldReset);
            return;
        }

        ClientContext.localPlayer.recenterOrigin(thisPlayer, shouldReset);
    }


    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", shift = At.Shift.AFTER, ordinal = 1), method = "sendPosition")
    public void visor$walkUp(CallbackInfo ci) {
        this.visor$teleported = false;
        if (VisorState.get().isNotActive()
                || !VRClientSettings.isWalkUpEnabled()) {
            return;
        }
        this.minecraft.options.autoJump().set(false);
    }





    // 1.21.11 Mth.sin/cos take a double
    //? if >=1.21.11 {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(D)F"), method = "updateAutoJump")
    private double visor$vrAutoJumpSin(double original) {
        return VisorState.get().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getBodyYaw()
                : original;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(D)F"), method = "updateAutoJump")
    private double visor$vrAutoJumpCos(double original) {
        return VisorState.get().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getBodyYaw()
                : original;
    }
    //?} else {
    /*@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"), method = "updateAutoJump")
    private float visor$vrAutoJumpSin(float original) {
        return VisorState.get().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getBodyYaw()
                : original;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(F)F"), method = "updateAutoJump")
    private float visor$vrAutoJumpCos(float original) {
        return VisorState.get().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getBodyYaw()
                : original;
    }
    *///?}


    /* ****************** *\
  //--------AIM PICK--------\\
    \* ****************** */
    // 1.21.11 moved the ray trace of GameRenderer.pick(Entity,DDF) into this static helper, see GameRendererMixin

    //? if >=1.21.11 {
    @ModifyVariable(at = @At("STORE"), method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", ordinal = 0)
    private static Vec3 visor$pickPos(Vec3 original) {
        return VRAimPicker.pickPos(original);
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", ordinal = 1)
    private static Vec3 visor$pickDirection(Vec3 original) {
        return VRAimPicker.pickDirection(original);
    }

    @WrapOperation(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;"))
    private static HitResult visor$vrBlockPick(Entity entity, double range, float partialTick, boolean fluid, Operation<HitResult> original) {
        HitResult vrHit = VRAimPicker.vrBlockPick();
        return vrHit != null ? vrHit : original.call(entity, range, partialTick, fluid);
    }
    //?}


    //? if >=1.21.2 {
    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lengthSquared(DDD)D"), method = "sendPosition")
    private double visor$directTeleport(double movedSquared) {
        return visor$sendTeleportMove(false) ? Double.MAX_VALUE : movedSquared;
    }
    //?} else {
    /*@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z"), ordinal = 2, method = "sendPosition")
    private boolean visor$directTeleport(boolean updateRotation) {
        return visor$sendTeleportMove(updateRotation);
    }
    *///?}

    @Unique
    private boolean visor$sendTeleportMove(boolean moved) {
        if (this.visor$teleported) {
            moved = true;
            ClientNetworking.sendVRPacket(
                    new TeleportMovePayloadToServer(
                            (float) this.getX(),
                            (float) this.getY(),
                            (float) this.getZ()
                    )
            );
        }
        return moved;
    }

    /**
     * Skips the outgoing position packet on the tick a VR teleport happened,
     * so the server does not flag the jump as illegal movement.
     */
    //? if >=1.21.2 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "sendPosition")
    public void visor$noPosPacketOnTeleport(ClientPacketListener instance, Packet<?> packet, Operation<Void> original) {
        if (!this.visor$teleported) {
            original.call(instance, packet);
        }
    }
    //?} else {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "sendPosition", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z")))
    public void visor$noPosPacketOnTeleport(ClientPacketListener instance, Packet<?> packet, Operation<Void> original) {
        if (!this.visor$teleported) {
            original.call(instance, packet);
        }
    }
    *///?}


    /* ************** *\
  //--------MISC--------\\
    \* ************** */

    @Override
    protected void visor$afterDie(DamageSource damageSource, CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isThisPlayerLocal(this)) {
            return;
        }
        ClientContext.inputManager
                .triggerHapticPulseBothMicroSec(HapticFeedback.DEATH);
    }


    @Inject(method = "getRopeHoldPosition", at = @At("HEAD"), cancellable = true)
    private void visor$ropeFromHand(CallbackInfoReturnable<Vec3> cir) {
        if (VisorState.get().isNotActive() || !visor$isThisPlayerLocal(this)) {
            return;
        }
        cir.setReturnValue(new Vec3((Vector3f) RenderPoseHelper.getHandPosition(HandType.MAIN)));
    }

    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */


    @Override
    @Unique
    public void visor$stepSound(BlockPos blockforNoise, Vec3 soundPos) {
        BlockState blockNoise = this.level().getBlockState(blockforNoise);
        Block block = blockNoise.getBlock();
        if (this.isSilent() || block.defaultBlockState().liquid()) {
            return;
        }

        BlockState blockAboveNoise = this.level().getBlockState(blockforNoise.above());
        SoundType soundType = blockNoise.getSoundType();
        if (blockAboveNoise.getBlock() == Blocks.SNOW) {
            soundType = blockAboveNoise.getSoundType();
        }

        SoundEvent soundevent = soundType.getStepSound();

        this.level().playSound(
                null,
                soundPos.x, soundPos.y, soundPos.z,
                soundevent,
                this.getSoundSource(),
                soundType.getVolume(),
                soundType.getPitch()
        );
    }

    @Override
    @Unique
    public void visor$setUsingItem(ItemStack item, InteractionHand hand) {
        this.useItem = item;

        if (item != ItemStack.EMPTY) {
            this.startedUsingItem = true;
            this.usingItemHand = hand;
        } else {
            this.startedUsingItem = false;
            this.usingItemHand = hand;
        }
    }

    @Override
    @Unique
    public double visor$getRoomYOffset() {
        return visor$roomYOffsetApplied;
    }

    @Unique
    private void visor$tickRoomYOffset() {
        double target = 0.0D;
        if (visor$isPoseModifyCamera()) {
            final double modifiedEyeHeight = 0.4D;
            var tickPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK);
            double eyeAboveOrigin = tickPose.getHmd().getPosition().y() - tickPose.getOrigin().y();
            target = modifiedEyeHeight - eyeAboveOrigin - ClientContext.localPlayer.getCameraOffsetWorld();
        }
        final double yStep = 0.06D;
        visor$roomYOffsetApplied += Mth.clamp(target - visor$roomYOffsetApplied, -yStep, yStep);
    }

    @Unique
    private boolean visor$isPoseModifyCamera() {
        Pose pose = this.getPose();
        return pose == Pose.SPIN_ATTACK
                || pose == Pose.FALL_FLYING
                || pose == Pose.SWIMMING && !ClientContext.localPlayer.isCrawling();
    }

    @Override
    @Unique
    public float visor$getSpeedFactor() {
        return this.visor$stuckSpeedMul.lengthSqr() > 0.0D
                ? (float) ((double) getBlockSpeedFactor()
                * (this.visor$stuckSpeedMul.x + this.visor$stuckSpeedMul.z) / 2.0D)
                : this.getBlockSpeedFactor();
    }

    @Override
    @Unique
    public float visor$getJumpFactor() {
        return this.visor$stuckSpeedMul.lengthSqr() > 0.0D
                ? (float) ((double) this.getBlockJumpFactor() * this.visor$stuckSpeedMul.y) :
                this.getBlockJumpFactor();
    }


    @Override
    @Unique
    public void visor$setUseItemRemaining(int count) {
        this.useItemRemaining = count;
    }

    @Override
    @Unique
    public void visor$setTeleported(boolean teleported) {
        this.visor$teleported = teleported;
    }


    /* ************************* *\
  //--------UTILITY METHODS--------\\
    \* ************************* */
    @Unique
    private boolean visor$isThisPlayerLocal(Object player) {
        if (LocalPlayer.class == player.getClass()) {
            return true;
        }
        return player == Minecraft.getInstance().player;
    }

    private LocalPlayer visor$getPlayer(){
        return (LocalPlayer) (Object) this;
    }
}

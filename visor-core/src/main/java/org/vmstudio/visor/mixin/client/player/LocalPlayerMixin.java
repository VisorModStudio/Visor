package org.vmstudio.visor.mixin.client.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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
    private Vec3 visor$moveMulIn = Vec3.ZERO;
    @Unique
    private boolean visor$walkUpBlocksActive = false;

    @Unique
    private boolean visor$teleported;

    @Shadow
    protected abstract void updateAutoJump(float f, float g);

    @Shadow
    public abstract void swing(InteractionHand interactionHand);



    /* ****************** *\
      //--------VEHICLE--------\\
        \* ****************** */
    @Inject(at = @At("TAIL"), method = "startRiding")
    public void visor$onStartRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.get().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        TaskVehicle.getInstance()
                .onStartRiding(
                        entity
                );

    }

    @Inject(at = @At("TAIL"), method = "removeVehicle")
    public void visor$onStopRiding(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isLocalPlayer(this)) {
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
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.localPlayer.updatePlayerLook(
                (LocalPlayer) (Object) this,
                PlayerPoseType.TICK
        );
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), method = "tick")
    public void visor$postTick(CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isLocalPlayer(this)) {
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
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.localPlayer.tickPlayer(
                visor$getPlayer()
        );
    }



    @Override
    protected void visor$wrapMove(MoverType type, Vec3 pos, Operation<Void> original) {
        if (VisorState.get().isNotActive()
                || !visor$isLocalPlayer(this)
                || Minecraft.getInstance().getCameraEntity() != visor$getPlayer()) {
            if (this.visor$walkUpBlocksActive) {
                setMaxUpStep(0.6F);
                this.visor$walkUpBlocksActive = false;
            }
            original.call(type, pos);
            return;
        }
        this.visor$moveMulIn = this.stuckSpeedMultiplier;

        if (pos.length() == 0 || this.isPassenger()) {
            original.call(type, pos);
            return;
        }

        boolean canMoveY = true;

        Vector3fc origin = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getOrigin();

        if ((this.zza != 0.0F
                || this.isFallFlying()
                || Math.abs(this.getDeltaMovement().x) > 0.0095
                || Math.abs(this.getDeltaMovement().z) > 0.0095
        )) {
            double xOffset = origin.x() - this.getX();
            double zOffset = origin.z() - this.getZ();
            double prevX = this.getX();
            double prevZ = this.getZ();

            if (VRClientSettings.isWalkUpEnabled()
                    && this.visor$walkUpBlocksActive
                    && visor$isApproachingInteractable(pos)) {
                this.setMaxUpStep(0.6F);
                this.visor$walkUpBlocksActive = false;
            }

            original.call(type, pos);

            if (VRClientSettings.isWalkUpEnabled()) {
                boolean smartBlocked = visor$isApproachingInteractable(this.getDeltaMovement());
                this.visor$walkUpBlocksActive = this.getBlockJumpFactor() == 1.0F
                        && !smartBlocked;
                this.setMaxUpStep(
                        this.visor$walkUpBlocksActive
                                ? 1.0F : 0.6F
                );
            } else {
                if (this.visor$walkUpBlocksActive) {
                    this.setMaxUpStep(0.6F);
                    this.visor$walkUpBlocksActive = false;
                }
                this.updateAutoJump(
                        (float) (this.getX() - prevX),
                        (float) (this.getZ() - prevZ)
                );
            }

            ClientContext.localPlayer.setOrigin(
                    (float) (this.getX() + xOffset),
                    (float) (this.getY() + this.visor$getRoomYOffset()),
                    (float) (this.getZ() + zOffset),
                    false
            );
        } else if (canMoveY) {
            original.call(type, new Vec3(0.0D, pos.y, 0.0D));
            ClientContext.localPlayer.setOrigin(
                    origin.x(),
                    (float) (this.getY() + this.visor$getRoomYOffset()),
                    origin.z(),
                    false
            );
        }else {
            this.setOnGround(true);
        }

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
        if (VisorState.get().isNotActive() || !visor$isLocalPlayer(this)) {
            original.call(amount, relative);
            return;
        }

        double speed = (relative.x * relative.x) + (relative.z * relative.z);
        if (speed < 0.0005) {
            return;
        }

        speed = Math.max(1, Math.sqrt(speed));

        speed = (double) amount / speed;
        Vec3 move = new Vec3(
                relative.x * speed,
                0.0D,
                relative.z * speed
        );


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

        // moveTo()/absMoveTo() snap xOld/yOld/zOld to the destination before
        // calling setPos(); regular movement leaves them at the start-of-tick
        // coords. Without resetting the room origin too, the entity body
        // renders at the destination while the VR camera is still lerping
        // from the old origin, showing the player's self-model "in the
        // future" for 1-2 frames after a teleport.
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





    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"), method = "updateAutoJump")
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


    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z"), ordinal = 2, method = "sendPosition")
    private boolean visor$directTeleport(boolean updateRotation) {
        if (this.visor$teleported) {
            updateRotation = true;
            ClientNetworking.sendVRPacket(
                    new TeleportMovePayloadToServer(
                            (float) this.getX(),
                            (float) this.getY(),
                            (float) this.getZ()
                    )
            );
        }
        return updateRotation;
    }

    /**
     * Skips the outgoing position packet on the tick a VR teleport happened,
     * so the server does not flag the jump as illegal movement.
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "sendPosition", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z")))
    public void visor$noPosPacketOnTeleport(ClientPacketListener instance, Packet<?> packet) {
        if (!this.visor$teleported) {
            instance.send(packet);
        }
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */

    @Override
    protected void visor$afterDie(DamageSource damageSource, CallbackInfo ci) {
        if (VisorState.get().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.inputManager
                .triggerHapticPulseBoth(2f);
    }


    @Inject(method = "getRopeHoldPosition", at = @At("HEAD"), cancellable = true)
    private void visor$vrRopePosition(CallbackInfoReturnable<Vec3> cir) {
        if (VisorState.get().isNotActive() || !visor$isLocalPlayer(this)) {
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
        SoundType soundType = block.getSoundType(blockNoise);
        if (blockAboveNoise.getBlock() == Blocks.SNOW) {
            soundType = Blocks.SNOW.getSoundType(blockAboveNoise);
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
        double out = 0.0D;

        if (this.getPose() == Pose.SPIN_ATTACK
                || this.getPose() == Pose.FALL_FLYING
                || this.getPose() == Pose.SWIMMING) {
            out = -0.01;
        }

        return out;
    }

    @Override
    @Unique
    public float visor$getSpeedFactor() {
        return this.visor$moveMulIn.lengthSqr() > 0.0D
                ? (float) ((double) getBlockSpeedFactor()
                * (this.visor$moveMulIn.x + this.visor$moveMulIn.z) / 2.0D)
                : this.getBlockSpeedFactor();
    }

    @Override
    @Unique
    public float visor$getJumpFactor() {
        return this.visor$moveMulIn.lengthSqr() > 0.0D
                ? (float) ((double) this.getBlockJumpFactor() * this.visor$moveMulIn.y) :
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
    private boolean visor$isLocalPlayer(Object player) {
        return player.getClass().equals(LocalPlayer.class)
                || Minecraft.getInstance().player == player;
    }

    private LocalPlayer visor$getPlayer(){
        return (LocalPlayer) (Object) this;
    }
}

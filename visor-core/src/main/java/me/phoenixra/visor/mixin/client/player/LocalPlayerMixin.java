package me.phoenixra.visor.mixin.client.player;

import com.mojang.authlib.GameProfile;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.input.HandAction;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.tasks.movement.vehicle.TaskRoomVehicle;
import me.phoenixra.visor.modified.client.entity.LocalPlayerModified;
import me.phoenixra.visor.modified.client.render.ItemInHandRendererModified;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements LocalPlayerModified {


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
    private boolean visor$initialized;
    @Unique
    private boolean visor$walkUpBlocksActive = false;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Shadow
    protected abstract void updateAutoJump(float f, float g);

    @Shadow
    public abstract void swing(InteractionHand interactionHand);






    /* ****************** *\
  //--------VEHICLE--------\\
    \* ****************** */
    @Inject(at = @At("TAIL"), method = "startRiding")
    public void visor$onStartRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        TaskRoomVehicle.getInstance()
                .onStartRiding(
                        entity
                );

    }

    @Inject(at = @At("TAIL"), method = "removeVehicle")
    public void visor$onStopRiding(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        TaskRoomVehicle.getInstance()
                .onStopRiding();
    }



     /* ****************** *\
   //--------MOVEMENT--------\\
     \* ****************** */

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.BEFORE), method = "tick")
    public void visor$preTick(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.localPlayer.updatePlayerLook(
                (LocalPlayer) (Object) this,
                PlayerPoseType.PREV_TICK
        );
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), method = "tick")
    public void visor$postTick(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.localPlayer.updatePlayerLook(
                (LocalPlayer) (Object) this,
                PlayerPoseType.PREV_TICK
        );
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"), method = "aiStep")
    public void visor$tickPlayer(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)
                || !visor$initialized) {
            return;
        }
        ClientContext.localPlayer.tickPlayer(
                (LocalPlayer) (Object) this
        );
    }

    /**
     * Updates client origin position on move
     *
     * @param info s
     */
    @Inject(at = @At("HEAD"), method = "move", cancellable = true)
    public void visor$onMove(MoverType type, Vec3 pos, CallbackInfo info) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)
                || Minecraft.getInstance().getCameraEntity() != this) {
            if (this.visor$walkUpBlocksActive) {
                this.setMaxUpStep(0.6F);
                this.visor$walkUpBlocksActive = false;
            }
            return;
        }
        info.cancel();
        // stuckSpeedMultiplier gets zeroed in the super call.
        this.visor$moveMulIn = this.stuckSpeedMultiplier;

        if (pos.length() == 0 || this.isPassenger()) {
            super.move(type, pos);
            return;
        }

        boolean canMoveY = true;

        Vector3fc origin = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getOrigin();

        if ((this.zza != 0.0F
                || this.isFallFlying()
                || Math.abs(this.getDeltaMovement().x) > 0.0095 ||
                Math.abs(this.getDeltaMovement().z) > 0.0095
        )) {
            double xOffset = origin.x() - this.getX();
            double zOffset = origin.z() - this.getZ();
            double prevX = this.getX();
            double prevZ = this.getZ();
            super.move(type, pos);

            if (VRClientSettings.isWalkUpEnabled()) {
                this.setMaxUpStep(this.getBlockJumpFactor() == 1.0F ? 1.0F : 0.6F);
                this.visor$walkUpBlocksActive = this.getBlockJumpFactor() == 1.0F;
            } else {
                if (this.visor$walkUpBlocksActive) {
                    this.setMaxUpStep(0.6F);
                    this.visor$walkUpBlocksActive = false;
                }
                this.updateAutoJump((float) (this.getX() - prevX), (float) (this.getZ() - prevZ));
            }

            ClientContext.localPlayer.setOrigin(
                    (float) (this.getX() + xOffset),
                    (float) (this.getY() + this.visor$getRoomYOffset()),
                    (float) (this.getZ() + zOffset),
                    false
            );
            return;
        }

        if (canMoveY) {
            super.move(type, new Vec3(0.0D, pos.y, 0.0D));
            ClientContext.localPlayer.setOrigin(
                    origin.x(),
                    (float) (this.getY() + this.visor$getRoomYOffset()),
                    origin.z(),
                    false
            );
            return;
        }

        this.setOnGround(true);


    }

    /**
     * VR Input movement
     */
    @Override
    public void moveRelative(float inputStrength, @NotNull Vec3 relative) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            super.moveRelative(inputStrength, relative);
            return;
        }

        double speed = (relative.x * relative.x) + (relative.z * relative.z);

        if (speed < 0.0005) {
            return;
        }

        speed = Math.max(
                1,
                Math.sqrt(speed)
        );

        speed = (double) inputStrength / speed;
        Vec3 move = new Vec3(
                relative.x * speed,
                0.0D,
                relative.z * speed
        );


        var rotationElement = ClientContext.localPlayer
                .getRotationElement(PlayerPoseType.PREV_TICK);

        //SWIMMING OR FLYING
        if (!this.isPassenger()
                && (this.isSwimming() || this.getAbilities().flying)) {

            move = move.xRot(
                    rotationElement.getPitch()
                            * ((float) Math.PI / 180F)
            );
        }
        move = move.yRot(
                rotationElement
                        .getYaw()
                        * ((float) Math.PI / 180F)
                        * -1
        );


        float yFactor = this.getAbilities().flying
                ? 5f
                : 1f;

        this.setDeltaMovement(
                this.getDeltaMovement().x + move.x,
                this.getDeltaMovement().y + move.y * (double) yFactor,
                this.getDeltaMovement().z + move.z
        );

    }


    /**
     * Updates client origin position
     * when instantly moved.
     * (Also called by mc on world join, so, we need
     * to make sure origin recenter to have right rendering pose)
     */
    @Override
    public void absMoveTo(double x, double e, double y, float g, float z) {
        super.absMoveTo(x, e, y, g, z);
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.localPlayer.recenterOrigin(
                (LocalPlayer) (Object) this,
                false
        );
    }


    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", shift = At.Shift.AFTER, ordinal = 1), method = "sendPosition")
    public void visor$walkUp(CallbackInfo ci) {
        if (VisorState.getState().isNotActive()
                || !VRClientSettings.isWalkUpEnabled()) {
            return;
        }
        this.minecraft.options.autoJump().set(false);
    }

    @Override
    public void setPos(double posX, double posY, double posZ) {
        this.visor$initialized = true;
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            super.setPos(posX, posY, posZ);
            return;
        }

        super.setPos(posX, posY, posZ);


        boolean shouldReset = (posX + posY + posZ) == 0;
        if (this.isPassenger()) {
            Vec3 premountPos = TaskRoomVehicle.getInstance().premountPosRoom;
            premountPos = premountPos
                    .yRot(
                            ClientContext.localPlayer
                                    .getPoseData(PlayerPoseType.PREV_TICK)
                                    .getRotationY()
                    );
            posX = posX - premountPos.x;
            posZ = posZ - premountPos.z;
            ClientContext.localPlayer.setOrigin(
                    (float) posX, (float) posY, (float) posZ,
                    shouldReset
            );
            return;
        }

        ClientContext.localPlayer.recenterOrigin(
                (LocalPlayer)(Object)this,
                shouldReset
        );

    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"), method = "updateAutoJump")
    private float visor$vrAutoJumpSin(float original) {
        return VisorState.getState().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.PREV_TICK).getBodyYaw()
                : original;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(F)F"), method = "updateAutoJump")
    private float visor$vrAutoJumpCos(float original) {
        return VisorState.getState().isActive()
                ? ClientContext.localPlayer
                .getPoseData(PlayerPoseType.PREV_TICK).getBodyYaw()
                : original;
    }





    /* ************** *\
  //--------MISC--------\\
    \* ************** */

    /**
     * Haptic feedback on death
     *
     * @param pCause s
     */
    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        ClientContext.inputManager
                .triggerHapticPulseBoth(2f);
    }


    @Inject(method = "getRopeHoldPosition", at = @At("HEAD"), cancellable = true)
    private void visor$vrRopePosition(CallbackInfoReturnable<Vec3> cir) {
        if (VisorState.getState().isNotActive()
                || !visor$isLocalPlayer(this)) {
            return;
        }
        cir.setReturnValue(
                new Vec3(
                        (Vector3f) RenderPoseHelper.getHandPosition(HandType.MAIN)
                )
        );
    }

    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */
    @Override
    @Unique
    public void visor$swingArm(InteractionHand interactionhand, HandAction interact) {
        ((ItemInHandRendererModified) this.minecraft
                .getEntityRenderDispatcher()
                .getItemInHandRenderer()
        ).visor$setSwingType(interact);
        this.swing(interactionhand);
    }

    @Override
    @Unique
    public void visor$stepSound(BlockPos blockforNoise, Vec3 soundPos) {

        BlockState blockNoise = this.level().getBlockState(
                blockforNoise
        );
        Block block = blockNoise.getBlock();
        if (this.isSilent()
                || block.defaultBlockState().liquid()) {
            return;
        }

        BlockState blockAboveNoise = this.level().getBlockState(
                blockforNoise.above()
        );

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
            out = -1.2D;
        }

        return out;
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


    /* ************************* *\
  //--------UTILITY METHODS--------\\
    \* ************************* */
    @Unique
    private boolean visor$isLocalPlayer(Object player) {
        return player.getClass().equals(LocalPlayer.class)
                || Minecraft.getInstance().player == player;
    }
}

package org.vmstudio.visor.mixin.client.world;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.movement.vehicle.TaskBoat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(Boat.class)
public abstract class BoatMixin extends Entity {

    @Shadow
    private float deltaRotation;
    @Shadow
    private boolean inputLeft;
    @Shadow
    private boolean inputRight;
    @Shadow
    private boolean inputUp;

    @Shadow
    public abstract void setPaddleState(boolean pLeft, boolean pRight);

    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 0), method = "controlBoat()V")
    public float visor$inputLeft(float f) {
        return MC.player.input.leftImpulse;
    }

    @ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 1), method = "controlBoat()V")
    public float visor$inputRight(float f) {
        return -MC.player.input.leftImpulse;
    }

    /**
     * Applying values received in TrackerBoat
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE), method = "controlBoat", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void visor$rowingInVR(CallbackInfo ci, float forward) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        ci.cancel();

        double momentumX, momentumZ;

        if (this.inputUp) {
            float yaw = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.TICK)
                    .getHand(HandType.OFFHAND).getYawDegrees();
            float end = this.getYRot() % 360;
            float start = yaw;
            float difference = Math.abs(end - start);

            if (difference > 180) {
                if (end > start) {
                    start += 360;
                } else {
                    end += 360;
                }
            }

            difference = end - start;

            forward = 0;

            if (Math.abs(difference) < 60) {
                forward = 0.06f;
            } else if (Math.abs(difference) > 150) {
                forward = -0.01F;
            } else if (difference < 0 || difference > 0) {
                forward = 0.008f;
            }
            this.deltaRotation = 0;

            momentumX = Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD) * forward;
            momentumZ = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * forward;
        } else {

            TaskBoat trackerBoat = TaskBoat.getInstance();
            if (trackerBoat.isRowing()) {
                this.deltaRotation += (float) (trackerBoat.getOarLeft() / 1.5);
                this.deltaRotation -= (float) (trackerBoat.getOarRight() / 1.5);

                if (deltaRotation < 0) {
                    this.inputLeft = true;
                }
                if (deltaRotation > 0) {
                    this.inputRight = true;
                }

                forward = Math.min(0.04F, 0.06f * trackerBoat.getMoveForward());
                if (forward > 0) {
                    this.inputUp = true;
                }

            }else {
                //to reduce rotation speed for controller movement
                //too fast rotation causes nausea
                deltaRotation = deltaRotation * 0.8f;
                forward = forward * 0.7f;
            }



            momentumX = Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD) * forward;
            momentumZ = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * forward;
        }
        this.setDeltaMovement(
                this.getDeltaMovement().x + momentumX,
                this.getDeltaMovement().y,
                this.getDeltaMovement().z + momentumZ
        );

        this.setPaddleState(
                this.inputRight && !this.inputLeft || this.inputUp,
                this.inputLeft && !this.inputRight || this.inputUp
        );
    }
}

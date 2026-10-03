package org.vmstudio.visor.mixin.client.world;

import org.vmstudio.visor.api.compatibility.mcversion.McPlayerInput;
import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.tasks.types.movement.vehicle.TaskBoat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
//? if >=1.21.2 {
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
//?} else {
/*import net.minecraft.world.entity.vehicle.Boat;
*///?}
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


//? if >=1.21.2 {
@Mixin(AbstractBoat.class)
//?} else {
/*@Mixin(Boat.class)
*///?}
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
    public float visor$analogTurnLeft(float vanillaStep) {
        return visor$hasAnalogSteering() ? McPlayerInput.leftImpulse(MC.player) : vanillaStep;
    }

    @ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 1), method = "controlBoat()V")
    public float visor$analogTurnRight(float vanillaStep) {
        return visor$hasAnalogSteering() ? -McPlayerInput.leftImpulse(MC.player) : vanillaStep;
    }

    @Unique
    private boolean visor$hasAnalogSteering() {
        return VisorState.get().isActive() && MC.player != null;
    }

    //keep @Local without variable name, to search by type
    //? if >=1.21.2 {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE), method = "controlBoat", cancellable = true)
    public void visor$rowingInVR(CallbackInfo ci, @Local float forward) {
        visor$applyRowing(ci, forward);
    }
    //?} else {
    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE), method = "controlBoat", cancellable = true)
    public void visor$rowingInVR(CallbackInfo ci, @Local float forward) {
        visor$applyRowing(ci, forward);
    }
    *///?}

    @Unique
    private void visor$applyRowing(CallbackInfo ci, float forward) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        ci.cancel();

        if (this.inputUp) {
            forward = visor$steerTowardsHand();
            this.deltaRotation = 0;
        } else {
            forward = visor$rowOars(forward);
        }

        float headingRad = this.getYRot() * Mth.DEG_TO_RAD;
        this.setDeltaMovement(
                this.getDeltaMovement().x + Mth.sin(-headingRad) * forward,
                this.getDeltaMovement().y,
                this.getDeltaMovement().z + Mth.cos(headingRad) * forward
        );

        this.setPaddleState(
                this.inputRight && !this.inputLeft || this.inputUp,
                this.inputLeft && !this.inputRight || this.inputUp
        );
    }

    @Unique
    private float visor$steerTowardsHand() {
        final float aheadConeDeg = 60.0F;
        final float behindConeDeg = 150.0F;
        final float aheadSpeed = 0.06F;
        final float reverseSpeed = -0.01F;
        final float pivotSpeed = 0.008F;

        float handYawDeg = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getHand(HandType.OFFHAND).getYawDegrees();
        float offHeadingDeg = Math.abs(Mth.wrapDegrees(this.getYRot() - handYawDeg));

        if (offHeadingDeg < aheadConeDeg) {
            return aheadSpeed;
        }
        if (offHeadingDeg > behindConeDeg) {
            return reverseSpeed;
        }
        return pivotSpeed;
    }

    @Unique
    private float visor$rowOars(float forward) {
        final double oarTurnDivisor = 1.5;
        final float oarForwardGain = 0.06F;
        // vanilla Boat.controlBoat adds 0.04F for a held forward input
        final float vanillaForwardSpeed = 0.04F;
        final float controllerTurnDamping = 0.8F;
        final float controllerForwardDamping = 0.7F;

        TaskBoat boat = TaskBoat.getInstance();
        if (!boat.isRowing()) {
            this.deltaRotation *= controllerTurnDamping;
            return forward * controllerForwardDamping;
        }

        this.deltaRotation += (float) ((boat.getOarLeft() - boat.getOarRight()) / oarTurnDivisor);
        this.inputLeft |= this.deltaRotation < 0;
        this.inputRight |= this.deltaRotation > 0;

        float rowed = Math.min(vanillaForwardSpeed, oarForwardGain * boat.getMoveForward());
        this.inputUp |= rowed > 0;
        return rowed;
    }
}

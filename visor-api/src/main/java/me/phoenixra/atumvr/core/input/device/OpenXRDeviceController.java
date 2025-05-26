package me.phoenixra.atumvr.core.input.device;

import lombok.Getter;

import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.device.VRDeviceController;

import me.phoenixra.atumvr.api.misc.pose.VRPose;
import me.phoenixra.atumvr.api.misc.pose.VRPoseMutable;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.input.action.types.HapticPulseAction;
import me.phoenixra.atumvr.core.input.action.types.multi.PoseMultiAction;
import org.jetbrains.annotations.NotNull;


public class OpenXRDeviceController extends OpenXRDevice implements VRDeviceController {
    @Getter
    private final ControllerType type;

    @Getter
    private boolean gripActive;

    private final VRPoseMutable gripPose = new VRPoseMutable();


    private final PoseMultiAction aimAction;
    private final PoseMultiAction gripAction;
    private final HapticPulseAction hapticPulseAction;

    public OpenXRDeviceController(OpenXRProvider provider,
                                  ControllerType controllerType,
                                  PoseMultiAction aimAction,
                                  PoseMultiAction gripAction,
                                  HapticPulseAction hapticPulseAction) {
        super(provider, controllerType==ControllerType.LEFT ? ID_LEFT : ID_RIGHT);
        this.type = controllerType;
        this.aimAction = aimAction;
        this.gripAction = gripAction;
        this.hapticPulseAction = hapticPulseAction;

    }

    @Override
    public void update() {
        var subActionAim = aimAction.getSubActions().get(type.ordinal());
        var subActionGrip = gripAction.getSubActions().get(type.ordinal());

        pose.update(subActionAim.getCurrentState());
        active = subActionAim.isActive();

        gripPose.update(subActionGrip.getCurrentState());
        gripActive = subActionGrip.isActive();



    }



    @Override
    public void triggerHapticPulse(float frequency, float amplitude, long durationNanoSec) {
        hapticPulseAction.triggerHapticPulse(type, frequency, amplitude, durationNanoSec);
    }


    @Override
    public @NotNull VRPose getGripPose() {
        return gripPose;
    }
}

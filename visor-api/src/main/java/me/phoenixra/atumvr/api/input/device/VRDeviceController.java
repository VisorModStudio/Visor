package me.phoenixra.atumvr.api.input.device;

import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.misc.pose.VRPose;
import org.jetbrains.annotations.NotNull;


public interface VRDeviceController extends VRDevice{
    String ID_LEFT = "controller_left";
    String ID_RIGHT = "controller_right";

    static String getDefaultId(@NotNull ControllerType type){
        return type == ControllerType.LEFT
                ? ID_LEFT
                : ID_RIGHT;
    }

    @NotNull
    ControllerType getType();

    @NotNull
    VRPose getGripPose();

    boolean isGripActive();



    default void triggerHapticPulse(float durationSeconds){
        triggerHapticPulse(
                160f,
                1f,
                (long) (durationSeconds * 1_000_000_000)
        );
    }

    default void triggerHapticPulse(float frequency, float amplitude,
                                    float durationSeconds){
        triggerHapticPulse(
                frequency,
                amplitude,
                (long) (durationSeconds * 1_000_000_000)
        );
    }

    void triggerHapticPulse(float frequency, float amplitude,
                            long durationNanoSec);
}

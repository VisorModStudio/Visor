package me.phoenixra.visor.api.client.input;


import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.ProfileSetHolder;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.ControllerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages VR input
 */
public interface InputManager {


    /**
     * Returns true if client has selected left-handed mode.
     * @return If left-handed
     */
    boolean isLeftHanded();

    VisorActionSet getActiveSet();

    @NotNull
    ProfileSetHolder getProfileSetHolder();

    @Nullable
    XRInteractionProfile getActiveProfile();


    /**
     * Trigger haptic pulse on both hands
     *
     * @param durationSeconds pulse duration in seconds
     */
    default void triggerHapticPulseBoth(float durationSeconds) {
        triggerHapticPulse(ControllerHand.MAIN, durationSeconds);
        triggerHapticPulse(ControllerHand.OFFHAND, durationSeconds);
    }

    /**
     * Trigger haptic pulse click on a specified hand
     *
     * @param hand the hand
     */
    default void triggerHapticPulseClick(@NotNull ControllerHand hand){
        triggerHapticPulse(
                hand,
                20f,
                0.2f,
                (long) (0.05f * 1_000_000_000)
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param durationSeconds pulse duration in seconds
     */
    default void triggerHapticPulse(@NotNull ControllerHand hand,
                                    float durationSeconds){
        triggerHapticPulse(
                hand,
                160f,
                1f,
                (long) (durationSeconds * 1_000_000_000)
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param durationMicroSeconds pulse duration in microseconds
     */
    default void triggerHapticPulseMicroSec(@NotNull ControllerHand hand,
                                            int durationMicroSeconds){
        triggerHapticPulse(
                hand,
                160f,
                1f,
                durationMicroSeconds * 1000L
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param frequency pulse frequency
     * @param amplitude pulse amplitude
     * @param durationSeconds pulse duration in seconds
     */
    default void triggerHapticPulse(@NotNull ControllerHand hand,
                                    float frequency,
                                    float amplitude,
                                    float durationSeconds){
        triggerHapticPulse(
                hand,
                frequency,
                amplitude,
                (long) (durationSeconds * 1_000_000_000)
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param frequency pulse frequency
     * @param amplitude pulse amplitude
     * @param durationMicroSeconds pulse duration in microseconds
     */
    default void triggerHapticPulseMicroSec(@NotNull ControllerHand hand,
                                            float frequency,
                                            float amplitude,
                                            int durationMicroSeconds){
        triggerHapticPulse(
                hand,
                frequency,
                amplitude,
                durationMicroSeconds * 1000L
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param frequency pulse frequency
     * @param amplitude pulse amplitude
     * @param durationNanoSec pulse duration in nanoseconds
     */
    void triggerHapticPulse(@NotNull ControllerHand hand,
                            float frequency,
                            float amplitude,
                            long durationNanoSec);
}

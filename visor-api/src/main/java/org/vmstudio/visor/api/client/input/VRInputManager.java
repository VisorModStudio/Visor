package org.vmstudio.visor.api.client.input;


import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.core.input.profile.XRProfileManager;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.common.HandType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages VR input
 */
public interface VRInputManager {


    /**
     * Returns true if client has selected left-handed mode.
     * @return If left-handed
     */
    boolean isLeftHanded();

    VRActionSet getActiveSet();

    @NotNull
    XRProfileManager getProfileManager();

    @Nullable
    VRInteractionProfileType getActiveProfile();


    /**
     * Trigger haptic pulse on both hands
     *
     * @param durationSeconds pulse duration in seconds
     */
    default void triggerHapticPulseBoth(float durationSeconds) {
        triggerHapticPulse(HandType.MAIN, durationSeconds);
        triggerHapticPulse(HandType.OFFHAND, durationSeconds);
    }

    /**
     * Trigger haptic pulse click on a specified hand
     *
     * @param hand the hand
     */
    default void triggerHapticPulseClick(@NotNull HandType hand){
        triggerHapticPulse(
                hand,
                160f,
                0.1f,
                (long) (0.0001f * 1_000_000_000)
        );
    }

    /**
     * Trigger haptic pulse on a specified hand
     *
     * @param hand the hand
     * @param durationSeconds pulse duration in seconds
     */
    default void triggerHapticPulse(@NotNull HandType hand,
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
    default void triggerHapticPulseMicroSec(@NotNull HandType hand,
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
    default void triggerHapticPulse(@NotNull HandType hand,
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
    default void triggerHapticPulseMicroSec(@NotNull HandType hand,
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
    void triggerHapticPulse(@NotNull HandType hand,
                            float frequency,
                            float amplitude,
                            long durationNanoSec);
}

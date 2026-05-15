package org.vmstudio.visor.api.client.input;


import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.core.input.profile.XRProfileManager;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
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


    @Nullable
    VRActionButton getActionLeftMouse(@NotNull HandType hand);
    @Nullable
    VRActionButton getActionRightMouse(@NotNull HandType hand);
    @Nullable
    VRActionButton getActionMiddleMouse(@NotNull HandType hand);
    @Nullable
    VRActionVec2 getActionScrollMouse(@NotNull HandType hand);

    /**
     * Trigger haptic pulse on both hands
     *
     * @param seconds pulse duration in seconds
     */
    default void triggerHapticPulseBoth(float seconds) {
        triggerHapticPulse(HandType.MAIN, seconds);
        triggerHapticPulse(HandType.OFFHAND, seconds);
    }

    /**
     * Trigger haptic pulse on both hands
     *
     * @param secondsMain pulse duration in seconds for Main hand
     * @param secondsOffhand pulse duration in seconds for Offhand
     */
    default void triggerHapticPulseBoth(float secondsMain, float secondsOffhand) {
        triggerHapticPulse(HandType.MAIN, secondsMain);
        triggerHapticPulse(HandType.OFFHAND, secondsOffhand);
    }

    /**
     * Trigger haptic pulse on both hands
     *
     * @param microseconds pulse duration in micro seconds
     */
    default void triggerHapticPulseBothMicroSec(int microseconds) {
        triggerHapticPulseMicroSec(HandType.MAIN, microseconds);
        triggerHapticPulseMicroSec(HandType.OFFHAND, microseconds);
    }

    /**
     * Trigger haptic pulse on both hands
     *
     * @param microsecondsMain pulse duration in micro seconds for Main hand
     * @param microsecondsOffhand pulse duration in micro seconds for Offhand
     */
    default void triggerHapticPulseBothMicroSec(int microsecondsMain, int microsecondsOffhand) {
        triggerHapticPulseMicroSec(HandType.MAIN, microsecondsMain);
        triggerHapticPulseMicroSec(HandType.OFFHAND, microsecondsOffhand);
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
     * @param durationMicroseconds pulse duration in microseconds
     */
    default void triggerHapticPulseMicroSec(@NotNull HandType hand,
                                            int durationMicroseconds){
        triggerHapticPulse(
                hand,
                160f,
                1f,
                durationMicroseconds * 1000L
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
     * @param durationMicroseconds pulse duration in microseconds
     */
    default void triggerHapticPulseMicroSec(@NotNull HandType hand,
                                            float frequency,
                                            float amplitude,
                                            int durationMicroseconds){
        triggerHapticPulse(
                hand,
                frequency,
                amplitude,
                durationMicroseconds * 1000L
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

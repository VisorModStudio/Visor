package me.phoenixra.visor.api.client.input;


import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Manages VR input
 */
public interface InputManager {


    /**
     * Get Action Set registry
     *
     * @return Action Set registry instance
     */
    @NotNull
    VisorElementRegistry<VisorActionSet> getActionSetRegistry();




    /**
     * Returns true if client has selected left-handed mode.
     * @return If left-handed
     */
    boolean isLeftHanded();



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
     * @param durationNanoSec pulse duration in nano seconds
     */
    void triggerHapticPulse(@NotNull ControllerHand hand,
                            float frequency,
                            float amplitude,
                            long durationNanoSec);
}

package me.phoenixra.visor.api.client.input;


import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import org.jetbrains.annotations.NotNull;

public interface InputManager {


    @NotNull
    VisorElementRegistry<VisorActionSet> getActionSetRegistry();




    /**
     * Returns true if client has selected left-handed mode.
     * @return If left-handed
     */
    boolean isLeftHanded();



    default void triggerHapticPulseBoth(float durationSeconds) {
        triggerHapticPulse(ControllerHand.MAIN, durationSeconds);
        triggerHapticPulse(ControllerHand.OFFHAND, durationSeconds);
    }

    default void triggerHapticPulseClick(ControllerHand hand){
        triggerHapticPulse(
                hand,
                20f,
                0.2f,
                (long) (0.05f * 1_000_000_000)
        );
    }
    default void triggerHapticPulse(ControllerHand hand,
                                    float durationSeconds){
        triggerHapticPulse(
                hand,
                160f,
                1f,
                (long) (durationSeconds * 1_000_000_000)
        );
    }

    default void triggerHapticPulse(ControllerHand hand,
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

    void triggerHapticPulse(ControllerHand hand,
                            float frequency,
                            float amplitude,
                            long durationNanoSec);
}

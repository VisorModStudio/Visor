package me.phoenixra.atumvr.core.input.action.profileset;

import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.types.HapticPulseAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.PoseMultiAction;
import org.jetbrains.annotations.NotNull;

public abstract class OpenXRProfileSet extends OpenXRActionSet {

    public OpenXRProfileSet(OpenXRProvider provider, String name, String localizedName, int priority) {
        super(provider, name, localizedName, priority);
    }

    @NotNull
    public abstract XRInteractionProfile getType();

    @NotNull
    public abstract FloatMultiAction getTriggerValue();


    public boolean isProfileActive(){
        return getTriggerValue().getHandSubaction(ControllerType.LEFT).isActive()
                || getTriggerValue().getHandSubaction(ControllerType.RIGHT).isActive();
    }
}

package me.phoenixra.atumvr.core.input.action.profileset;

import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class OpenXRProfileSet extends OpenXRActionSet {

    public OpenXRProfileSet(OpenXRProvider provider, String name, String localizedName, int priority) {
        super(provider, name, localizedName, priority);
    }

    @NotNull
    public abstract XRInteractionProfile getType();

    @NotNull
    public abstract FloatButtonMultiAction getTriggerValue();


    public abstract Collection<String> getButtonIds();
    public abstract @Nullable VRActionDataButton getButton(@NotNull String id);

    public abstract Collection<String> getVec2Ids();
    public abstract @Nullable VRActionDataVec2 getVec2(@NotNull String id);


    public boolean isProfileActive(){
        return getTriggerValue().getHandSubaction(ControllerType.LEFT).isActive()
                || getTriggerValue().getHandSubaction(ControllerType.RIGHT).isActive();
    }
}

package me.phoenixra.visor.api.client.input.action;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface VisorAction {


    void preTick();


    void updateState(OpenXRProfileSet currentProfile,
                     boolean leftHanded);

    boolean isActive();

    boolean isChanged();



    @Nullable
    BindingPath getBinding(XRInteractionProfile profile);

    @Nullable
    BindingPath getDefaultBinding(XRInteractionProfile profile);


    @NotNull
    VisorActionSet getActionSet();


    @NotNull
    String getId();

}

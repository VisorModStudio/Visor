package me.phoenixra.visor.api.client.input.action;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface VisorAction {


    void preTick();


    void updateState(@NotNull OpenXRProfileSet currentProfile,
                     boolean leftHanded);

    void clear();

    boolean isActive();

    boolean isChanged();


    void setBinding(@NotNull XRInteractionProfile profile,
                    @NotNull BindingPath path);

    @Nullable
    BindingPath getBinding(@NotNull XRInteractionProfile profile);

    @NotNull
    default BindingPath getBindingOrEmpty(@NotNull XRInteractionProfile profile){
        return Optional.ofNullable(getBinding(profile)).orElse(BindingPath.EMPTY);
    }

    @Nullable
    BindingPath getDefaultBinding(@NotNull XRInteractionProfile profile);

    @NotNull
    Collection<String> getSelectableBindings(@NotNull XRInteractionProfile profile);

    @NotNull
    VisorActionSet getActionSet();


    @NotNull
    default Component getName(){
        return Component.translatable("visor.action."+getId());
    }

    @NotNull
    String getId();

}

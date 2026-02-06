package me.phoenixra.visor.api.client.input.action.framework;


import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataButton;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public abstract class VisorActionButton implements VisorAction {

    @Getter
    private final VisorActionSet actionSet;

    @Getter
    private final String id;

    @Getter
    protected boolean active;

    @Getter
    protected boolean changed;

    @Getter
    protected boolean pressed = false;


    protected boolean pressDelayed;

    protected boolean releaseDelayed;

    protected boolean forcedState;



    protected final Map<VRInteractionProfileType, ActionBinding> defaultBindings;

    protected Map<VRInteractionProfileType, ActionBinding> bindings;


    public VisorActionButton(VisorActionSet actionSet,
                             String id
    ) {
        this.actionSet = actionSet;
        this.id = id;
        this.defaultBindings = new EnumMap<>(loadDefaults());
        this.bindings = new EnumMap<>(defaultBindings);
    }

    protected abstract Map<VRInteractionProfileType, ActionBinding> loadDefaults();

    protected abstract void onPress();

    protected abstract void onRelease();

    protected void onClear(){

    }


    protected @Nullable VRActionDataButton getButtonData(@NotNull ActionBinding actionBinding,
                                                         @NotNull XRInteractionProfile currentProfile,
                                                         boolean leftHanded){
        return actionBinding.getButton(currentProfile, leftHanded);
    }

    @Override
    public void preTick() {
        if(pressDelayed && !pressed){
            pressed = true;
            pressDelayed = false;
            changed = true;
            onPress();
            return;
        }
        if(releaseDelayed && pressed){
            forcedState = false;
            pressed = false;
            releaseDelayed = false;
            changed = true;
            onRelease();
            return;
        }
        changed = false;


    }

    @Override
    public void updateState(@NotNull XRInteractionProfile currentProfile, boolean leftHanded) {
        ActionBinding actionBinding = bindings.get(currentProfile.getType());

        if(actionBinding == null){
            active = false;
            if(pressed){
                releaseDelayed = true;
                pressDelayed = false;
            }
            return;
        }

        if(forcedState){
            return;
        }

        var buttonData = getButtonData(actionBinding, currentProfile, leftHanded);

        if(buttonData == null){
            if(active) {
                clear();
            }
            return;
        }

        active = buttonData.isActive();

        if(!active){
            if(pressed){
                releaseDelayed = true;
                pressDelayed = false;
            }
            return;
        }


        if(!buttonData.isButtonChanged()){
            return;
        }

        if(buttonData.isPressed()){
            pressed = false;
            pressDelayed = true;
            releaseDelayed = false;
        }else if(pressed){
            releaseDelayed = true;
        }

    }

    @Override
    public void clear(){
        if(pressed){
            pressed = false;
            releaseDelayed = false;
            changed = true;
            onRelease();
        }

        pressed = false;
        active = false;
        releaseDelayed = false;
        pressDelayed = false;
        changed = false;

        onClear();
    }

    public void forcePress(){
        forcedState = true;
        pressDelayed = true;
        releaseDelayed = false;
    }
    public void forceRelease(){
        pressDelayed = false;
        releaseDelayed = true;
    }

    public void setBinding(@NotNull VRInteractionProfileType profile, @NotNull ActionBinding binding){
        bindings.put(profile, binding);
    }

    @Override
    public @Nullable ActionBinding getBinding(@NotNull VRInteractionProfileType profile) {
        return bindings.get(profile);
    }

    @Override
    public @Nullable ActionBinding getDefaultBinding(@NotNull VRInteractionProfileType profile) {
        return defaultBindings.get(profile);
    }


    @Override
    public @NotNull Collection<VRActionIdentifier> getSupportedBindingIds(@NotNull VRInteractionProfileType profile) {
        var profileSet = VisorAPI.client().getInputManager()
                .getProfileManager()
                .getProfile(profile);

        var out = new ArrayList<VRActionIdentifier>();
        out.add(ActionBinding.EMPTY_ID);
        if(profileSet != null){
            out.addAll(profileSet.getButtonIds());
        }
        return out;
    }

}

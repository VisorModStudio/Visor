package me.phoenixra.visor.api.client.input.action.types;


import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;


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

    protected final Map<XRInteractionProfile, BindingPath> defaultBindings;

    protected final Map<XRInteractionProfile, BindingPath> bindings;


    public VisorActionButton(VisorActionSet actionSet,
                             String id
    ) {
        this.actionSet = actionSet;
        this.id = id;
        this.defaultBindings = new EnumMap<>(loadDefaults());
        this.bindings = new EnumMap<>(defaultBindings);
    }

    protected abstract Map<XRInteractionProfile, BindingPath> loadDefaults();

    protected abstract void onPress();

    protected abstract void onRelease();

    protected void onClear(){

    }


    protected VRActionDataButton getButtonData(@NotNull BindingPath bindingPath,
                                               @NotNull OpenXRProfileSet currentProfile,
                                               boolean leftHanded){
        return bindingPath.getButton(currentProfile, leftHanded);
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
    public void updateState(OpenXRProfileSet currentProfile, boolean leftHanded) {
        BindingPath bindingPath = bindings.get(currentProfile.getType());

        if(bindingPath == null){
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

        var buttonData = getButtonData(bindingPath, currentProfile, leftHanded);

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

    @Override
    public @Nullable BindingPath getBinding(XRInteractionProfile profile) {
        return bindings.get(profile);
    }

    @Override
    public @Nullable BindingPath getDefaultBinding(XRInteractionProfile profile) {
        return defaultBindings.get(profile);
    }
}

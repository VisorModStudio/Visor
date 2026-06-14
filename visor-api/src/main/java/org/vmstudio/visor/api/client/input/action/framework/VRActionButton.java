package org.vmstudio.visor.api.client.input.action.framework;


import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataButton;
import me.phoenixra.atumvr.api.input.profile.types.*;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.client.input.action.ActionKeyModifierType;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public abstract class VRActionButton implements VRAction {

    @Getter
    private final VRActionSet actionSet;

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


    public VRActionButton(@NotNull VRActionSet actionSet,
                          @NotNull String id
    ) {
        this.actionSet = actionSet;
        this.id = id.toLowerCase();
        this.defaultBindings = new EnumMap<>(VRInteractionProfileType.class);
        this.defaultBindings.putAll(getDefaultBindings());
        this.bindings = new EnumMap<>(defaultBindings);
    }


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
            press();
            return;
        }
        if(releaseDelayed && pressed){
            forcedState = false;
            pressed = false;
            releaseDelayed = false;
            changed = true;
            release();
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

        var keyModifier = actionBinding.getActionKeyModifier(leftHanded);

        if(actionSet.isKeyModifiersActive(currentProfile.getType())) {
            boolean leftPressed = ActionBinding.getKeyModifier(currentProfile, ControllerType.LEFT).isPressed();
            boolean rightPressed = ActionBinding.getKeyModifier(currentProfile, ControllerType.RIGHT).isPressed();
            if(leftPressed && rightPressed){
                return;
            }
            if(!leftPressed && keyModifier == ActionKeyModifierType.LEFT_TRIGGER){
                return;
            }
            if(!rightPressed && keyModifier == ActionKeyModifierType.RIGHT_TRIGGER){
                return;
            }
            if((leftPressed || rightPressed) && keyModifier == ActionKeyModifierType.OFF){
                return;
            }
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
        if (pressed && !buttonData.isPressed()) {
            releaseDelayed = true;
            pressDelayed = false;
            return;
        }

        if(!buttonData.isButtonChanged()){
            return;
        }

        if (buttonData.isPressed()) {
            //release first, if wasn't for some reason
            if (pressed) {
                releaseDelayed = true;
            }
            pressDelayed = true;
        }

    }

    @Override
    public void clear(){
        if(pressed){
            pressed = false;
            releaseDelayed = false;
            changed = true;
            release();
        }

        pressed = false;
        active = false;
        releaseDelayed = false;
        pressDelayed = false;
        changed = false;
        forcedState = false;

        onClear();
    }

    protected void press(){
        var event = new ActionButtonVREvent(this, true);
        VisorAPI.eventBus().callEvent(event);
        if(!event.isCanceled()) {
            onPress();
        }
    }
    protected void release(){
        var event = new ActionButtonVREvent(this, false);
        VisorAPI.eventBus().callEvent(event);
        if(!event.isCanceled()) {
            onRelease();
        }
    }

    public void forcePress(){
        forcedState = true;
        pressDelayed = true;
        releaseDelayed = false;
    }
    public void forceRelease(){
        pressDelayed = false;
        releaseDelayed = true;
        if (!pressed) {
            forcedState = false;
        }
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
    public @NotNull Collection<VRActionIdentifier> getSupportedBindingIds(@NotNull VRInteractionProfileType profileType,
                                                                          boolean keyModifiersActive) {
        var list = new ArrayList<VRActionIdentifier>();
        list.add(ActionBinding.ID_EMPTY);
        list.addAll(switch (profileType){
            case VALVE_INDEX -> ValveIndexProfile.BUTTON_IDS;
            case OCULUS_TOUCH -> OculusTouchProfile.BUTTON_IDS;
            case VIVE -> ViveProfile.BUTTON_IDS;
            case VIVE_COSMOS -> ViveCosmosProfile.BUTTON_IDS;
            case HP_MIXED_REALITY -> HpMixedRealityProfile.BUTTON_IDS;
            case WINDOWS_MOTION -> WindowsMotionProfile.BUTTON_IDS;
        });
        if(keyModifiersActive){
            list.removeIf(it-> it.getValue().contains("trigger"));
        }
        return list;
    }

}

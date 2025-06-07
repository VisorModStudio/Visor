package me.phoenixra.visor.api.client.input.action.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.EnumMap;
import java.util.Map;

public abstract class VisorActionVec2 implements VisorAction {
    @Getter
    private final VisorActionSet actionSet;

    @Getter
    private final String id;

    @Getter
    private boolean active;

    @Getter
    private boolean changed;

    @Getter
    private Vector2f state = new Vector2f();




    private final Map<XRInteractionProfile, BindingPath> defaultBindings;

    private final Map<XRInteractionProfile, BindingPath> bindings;


    public VisorActionVec2(VisorActionSet actionSet,
                           String id
    ) {
        this.actionSet = actionSet;
        this.id = id;

        this.defaultBindings = new EnumMap<>(loadDefaults());
        this.bindings = new EnumMap<>(defaultBindings);

    }

    protected abstract Map<XRInteractionProfile, BindingPath> loadDefaults();

    protected abstract void onStateChanged(Vector2f newState);

    protected void onClear(){

    }

    @Override
    public void preTick() {
        if(changed) {
            onStateChanged(state);
            changed = false;
        }
    }

    @Override
    public void updateState(OpenXRProfileSet currentProfile, boolean leftHanded) {
        BindingPath bindingPath = bindings.get(currentProfile.getType());
        if(bindingPath == null){
            active = false;
            changed = true;
            state.set(0,0);
            return;
        }

        var vec2Data = bindingPath.getVec2(
                currentProfile, leftHanded
        );

        active = vec2Data.isActive();
        if(!active){
            return;
        }

        if(!vec2Data.isChanged()){
            return;
        }
        changed = true;
        state = vec2Data.getCurrentState();


    }
    @Override
    public void clear(){
        changed = true;
        state.set(0,0);
        onStateChanged(state);

        active = false;
        changed = false;

        onClear();
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

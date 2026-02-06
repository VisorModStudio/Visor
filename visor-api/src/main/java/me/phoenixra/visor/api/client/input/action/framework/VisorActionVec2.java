package me.phoenixra.visor.api.client.input.action.framework;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.*;

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




    protected final Map<VRInteractionProfileType, ActionBinding> defaultBindings;

    protected final Map<VRInteractionProfileType, ActionBinding> bindings;


    public VisorActionVec2(VisorActionSet actionSet,
                           String id
    ) {
        this.actionSet = actionSet;
        this.id = id;

        this.defaultBindings = new EnumMap<>(loadDefaults());
        this.bindings = new EnumMap<>(defaultBindings);

    }

    protected abstract Map<VRInteractionProfileType, ActionBinding> loadDefaults();

    protected abstract void onStateChanged(Vector2f newState);

    protected void onClear(){

    }

    protected @Nullable VRActionDataVec2 getVec2Data(@NotNull ActionBinding actionBinding,
                                                     @NotNull XRInteractionProfile currentProfile,
                                                     boolean leftHanded){
        return actionBinding.getVec2(currentProfile, leftHanded);
    }

    @Override
    public void preTick() {
        if(changed) {
            onStateChanged(state);
            changed = false;
        }
    }

    @Override
    public void updateState(@NotNull XRInteractionProfile currentProfile, boolean leftHanded) {
        ActionBinding actionBinding = bindings.get(currentProfile.getType());
        if(actionBinding == null){
            active = false;
            changed = true;
            state.set(0,0);
            return;
        }

        var vec2Data = getVec2Data(
                actionBinding,
                currentProfile,
                leftHanded
        );
        if(vec2Data == null){
            if(active) {
                clear();
            }
            return;
        }

        active = vec2Data.isActive();
        if(!active){
            return;
        }

        if(!vec2Data.isChanged()){
            return;
        }
        changed = true;
        state = vec2Data.getVec2Data();


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
            out.addAll(profileSet.getVec2Ids());
        }
        return out;
    }
}

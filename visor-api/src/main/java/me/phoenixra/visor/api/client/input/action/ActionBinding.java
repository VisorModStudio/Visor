package me.phoenixra.visor.api.client.input.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds action identifiers for left/right-handed bindings.
 */
@Data @AllArgsConstructor
public class ActionBinding {
    public static final VRActionIdentifier EMPTY_ID = new VRActionIdentifier("null");
    public static final ActionBinding EMPTY = new ActionBinding(EMPTY_ID, EMPTY_ID);

    private VRActionIdentifier rightHandedId;
    private VRActionIdentifier leftHandedId;

    /**
     * Sets the binding for the specified handedness.
     */
    public void setActionId(@NotNull VRActionIdentifier identifier,
                            boolean leftHanded){
        if(leftHanded){
            leftHandedId = identifier;
        }else{
            rightHandedId = identifier;
        }
    }

    /**
     * Gets the left/right-handed binding action id
     */
    public VRActionIdentifier getActionId(boolean leftHanded){
        return leftHanded ? leftHandedId : rightHandedId;
    }

    /**
     * Resolves a button action from the specified profile.
     */
    public @Nullable VRActionDataButton getButton(@NotNull XRInteractionProfile profile,
                                                  boolean leftHanded){
        return profile.getButton(
                getActionId(leftHanded)
        );
    }

    /**
     * Resolves a vec2 action from the specified profile.
     */
    public @Nullable VRActionDataVec2 getVec2(@NotNull XRInteractionProfile profile,
                                              boolean leftHanded){
        return profile.getVec2(
                getActionId(leftHanded)
        );
    }


}

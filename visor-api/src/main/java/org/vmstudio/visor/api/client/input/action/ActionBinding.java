package org.vmstudio.visor.api.client.input.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.data.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds action identifiers for left/right-handed bindings.
 */
@Data @AllArgsConstructor
public class ActionBinding {
    public static final VRActionIdentifier ID_EMPTY = new VRActionIdentifier("null");
    public static final ActionBinding EMPTY = new ActionBinding(ID_EMPTY, ID_EMPTY);


    private ActionKeyModifierType rightHandedKeyModifier;
    private VRActionIdentifier rightHandedId;
    private ActionKeyModifierType leftHandedKeyModifier;
    private VRActionIdentifier leftHandedId;

    public ActionBinding(@NotNull VRActionIdentifier rightHandedId,
                          @NotNull VRActionIdentifier leftHandedId){
        this.rightHandedKeyModifier = ActionKeyModifierType.OFF;
        this.rightHandedId = rightHandedId;
        this.leftHandedKeyModifier = ActionKeyModifierType.OFF;
        this.leftHandedId = leftHandedId;
    }
    public ActionBinding(@NotNull ActionBinding binding){
        this.rightHandedKeyModifier = binding.getRightHandedKeyModifier();
        this.rightHandedId = binding.getRightHandedId();
        this.leftHandedKeyModifier = binding.getLeftHandedKeyModifier();
        this.leftHandedId = binding.getLeftHandedId();
    }

    public Component getActionDisplayName(boolean leftHanded){
        return getActionDisplayName(getActionId(leftHanded));
    }
    /**
     * Sets the key modifier type for the specified handedness.
     */
    public void setActionKeyModifier(@NotNull ActionKeyModifierType keyModifier,
                                     boolean leftHanded){
        if(leftHanded){
            leftHandedKeyModifier = keyModifier;
        }else{
            rightHandedKeyModifier = keyModifier;
        }
    }

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
     * Gets the left/right-handed binding action key modifier type
     */
    public ActionKeyModifierType getActionKeyModifier(boolean leftHanded){
        return leftHanded ? leftHandedKeyModifier : rightHandedKeyModifier;
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

    public static VRActionDataButton getKeyModifier(@NotNull XRInteractionProfile profile,
                                                    @NotNull ControllerType controllerType){
        return profile.getTriggerButton(controllerType);
    }

    public static Component getActionDisplayName(VRActionIdentifier actionId){
        var actionIdValue = actionId.getValue();
        if(actionId.equals(ID_EMPTY)){
            return Component.translatable("visor.action.bindings.null");
        }
        Component component;
        if(actionIdValue.contains(".touch")){
            actionIdValue = actionIdValue
                    .replace(".touch","");
            component = Component.translatable("visor.action.bindings."+actionIdValue);
            return Component.literal(
                    component.getString()
                            + " " +
                            Component.translatable("visor.action.bindings.touch").getString()
            );
        }
        if(actionIdValue.contains(".force")){
            actionIdValue = actionIdValue
                    .replace(".force","");
            component = Component.translatable("visor.action.bindings."+actionIdValue);

            return Component.literal(
                    component.getString()
                            + " " +
                            Component.translatable("visor.action.bindings.force").getString()
            );
        }
        return Component.translatable("visor.action.bindings."+actionIdValue);
    }
}

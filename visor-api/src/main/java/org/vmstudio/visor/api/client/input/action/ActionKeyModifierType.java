package org.vmstudio.visor.api.client.input.action;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum ActionKeyModifierType {
    OFF(Component.translatable("visor.action.options.enum.ActionKeyModifierType.OFF")),
    LEFT_TRIGGER(Component.translatable("visor.action.options.enum.ActionKeyModifierType.LEFT_TRIGGER")),
    RIGHT_TRIGGER(Component.translatable("visor.action.options.enum.ActionKeyModifierType.RIGHT_TRIGGER"));

    @Getter
    private final Component displayName;
    ActionKeyModifierType(Component displayName){
        this.displayName = displayName;
    }
    public static boolean isKeyModifierPressed(@NotNull XRInteractionProfile currentProfile,
                                               @NotNull ActionBinding binding,
                                               boolean leftHanded){
        ActionKeyModifierType modifierType = binding.getActionKeyModifier(leftHanded);
        if(modifierType == ActionKeyModifierType.LEFT_TRIGGER){
            return ActionBinding.getKeyModifier(currentProfile, ControllerType.LEFT).isPressed();
        }
        if(modifierType == ActionKeyModifierType.RIGHT_TRIGGER){
            return ActionBinding.getKeyModifier(currentProfile, ControllerType.RIGHT).isPressed();
        }
        return false;
    }
}

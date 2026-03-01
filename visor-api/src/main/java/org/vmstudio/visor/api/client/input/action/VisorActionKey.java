package org.vmstudio.visor.api.client.input.action;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.framework.VisorActionButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Visor action that types a single character.
 * <p>
 *     Users can create custom actions in settings based on this.
 * </p>
 */
public class VisorActionKey extends VisorActionButton {


    @Getter
    private final boolean required = false;

    @Getter
    private final char character;

    private final Component name;
    @Getter
    private final String nameKey;

    public VisorActionKey(@NotNull String id,
                          @NotNull VisorActionSet actionSet,
                          char character,
                          @NotNull String nameKey) {
        super(actionSet, id);
        this.character = character;
        this.name = Component.translatable(nameKey);
        this.nameKey = nameKey;
    }


    @Override
    protected void onPress() {
        InputHelper.typeChar(character);
    }

    @Override
    protected void onRelease() {

    }


    @Override
    public @NotNull Component getName() {
        return name;
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        //empty
        return Map.of();
    }

}

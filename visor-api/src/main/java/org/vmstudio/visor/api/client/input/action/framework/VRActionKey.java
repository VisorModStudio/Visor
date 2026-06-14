package org.vmstudio.visor.api.client.input.action.framework;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * VR action that types a single character.
 * <p>
 *     Users can create custom actions in settings based on this.
 * </p>
 */
public class VRActionKey extends VRActionButton {


    @Getter
    private final boolean required = false;

    @Getter
    private final char character;

    private final Component name;
    @Getter
    private final String nameKey;

    public VRActionKey(@NotNull String id,
                       @NotNull VRActionSet actionSet,
                       char character,
                       @NotNull String nameKey) {
        super(actionSet, id);
        this.character = character;
        this.name = Component.translatable(nameKey);
        this.nameKey = nameKey;
    }


    @Override
    protected void onPress() {
        InputHelper.pressChar(character);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseChar(character);
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

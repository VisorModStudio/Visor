package org.vmstudio.visor.core.client.input.actions.game;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.core.client.input.actions.KeyAction;

import java.util.Map;

public class GameActionInventory extends KeyAction {
    public static final String ID = "key.inventory";

    public GameActionInventory(@NotNull VRActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of();
    }
}

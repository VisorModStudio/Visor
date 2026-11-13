package org.vmstudio.visor.core.client.input.actions.game;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class GameActionSprint extends VRActionButton {
    public static final String ID = "sprint";



    public GameActionSprint(VRActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        InputHelper.updateKeyMapping(MC.options.keySprint, true);
    }

    @Override
    protected void onRelease() {
        InputHelper.updateKeyMapping(MC.options.keySprint, false);
    }



    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of();
    }

}

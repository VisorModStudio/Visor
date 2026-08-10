package org.vmstudio.visor.core.client.input.actions;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ActionScreenshot extends VRActionButton {
    public static final String ID = "screenshot";

    public ActionScreenshot(VRActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        ClientContext.renderer.setAskedForScreenShot(true);
    }

    @Override
    protected void onRelease() {

    }


    @Override
    public boolean isCommon() {
        return true;
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of();
    }


}

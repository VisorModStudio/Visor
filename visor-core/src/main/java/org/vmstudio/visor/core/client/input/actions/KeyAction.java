package org.vmstudio.visor.core.client.input.actions;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;

public abstract class KeyAction extends VRActionButton {
    private final String keyId;

    public KeyAction(@NotNull VRActionSet actionSet, @NotNull String keyId) {
        this(actionSet, keyId, keyId);
    }

    public KeyAction(@NotNull VRActionSet actionSet, @NotNull String id, @NotNull String keyId) {
        super(actionSet, id);
        this.keyId = keyId;
    }

    @Override
    protected void onPress() {
        InputHelper.pressKey(this.keyId);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseKey(this.keyId);
    }
}

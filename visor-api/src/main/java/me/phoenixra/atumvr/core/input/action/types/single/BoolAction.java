package me.phoenixra.atumvr.core.input.action.types.single;

import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateBoolean;
import org.lwjgl.system.MemoryStack;

public class BoolAction extends OpenXRSingleAction<Boolean> {


    public BoolAction(OpenXRProvider provider,
                      OpenXRActionSet actionSet,
                      String name,
                      String localizedName) {
        super(provider, actionSet, name, localizedName, XRInputActionType.BOOLEAN);
        currentState = false;
    }

    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }
    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            var state = XrActionStateBoolean.calloc(stack)
                    .type(actionType.getStateId());
            getInfo.action(handle);
            provider.checkXRError(
                    XR10.xrGetActionStateBoolean(
                            provider.getState().getVrSession().getHandle(),
                            getInfo,
                            state
                    ),
                    "xrGetActionStateFloat"
            );
            this.currentState = state.currentState();
            this.changed = state.changedSinceLastSync();
            this.lastChangeTime = state.lastChangeTime();
            this.active = state.isActive();
        }
    }
}

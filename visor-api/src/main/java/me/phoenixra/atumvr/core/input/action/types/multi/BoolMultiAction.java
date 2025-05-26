package me.phoenixra.atumvr.core.input.action.types.multi;

import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateBoolean;
import org.lwjgl.system.MemoryStack;

import java.util.List;

public class BoolMultiAction extends OpenXRMultiAction<Boolean> {

    public BoolMultiAction(OpenXRProvider provider,
                           OpenXRActionSet actionSet,
                           String name,
                           String localizedName,
                           List<SubAction<Boolean>> subActions) {
        super(provider, actionSet, name, localizedName, XRInputActionType.BOOLEAN, subActions);
    }

    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for(SubAction<Boolean> entry : subActions) {
                var state = XrActionStateBoolean.calloc(stack)
                        .type(actionType.getStateId());
                getInfo.subactionPath(entry.getPathHandle());
                getInfo.action(handle);
                provider.checkXRError(
                        XR10.xrGetActionStateBoolean(
                                provider.getState().getVrSession().getHandle(),
                                getInfo,
                                state
                        ),
                        "xrGetActionStateFloat"
                );
                entry.update(
                        state.currentState(),
                        state.lastChangeTime(),
                        state.changedSinceLastSync(),
                        state.isActive()
                );
            }
        }
    }
}

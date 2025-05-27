package me.phoenixra.atumvr.core.input.action.types.multi;

import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateFloat;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloatMultiAction extends OpenXRMultiAction<Float> {




    public FloatMultiAction(OpenXRProvider provider,
                            OpenXRActionSet actionSet,
                            String id,
                            String localizedName,
                            List<SubAction<Float>> subActions) {
        super(provider, actionSet, id, localizedName, XRInputActionType.FLOAT, subActions);
    }


    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for(SubAction<Float> entry : subActions) {
                var state = XrActionStateFloat.calloc(stack)
                        .type(actionType.getStateId());
                getInfo.subactionPath(entry.getPathHandle());
                getInfo.action(handle);
                provider.checkXRError(
                        XR10.xrGetActionStateFloat(
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

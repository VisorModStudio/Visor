package me.phoenixra.atumvr.core.input.action.types.multi;

import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.joml.Vector2f;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateVector2f;
import org.lwjgl.system.MemoryStack;

import java.util.List;

public class Vec2MultiAction extends OpenXRMultiAction<Vector2f> {

    public Vec2MultiAction(OpenXRProvider provider,
                           OpenXRActionSet actionSet,
                           String name,
                           String localizedName,
                           List<SubAction<Vector2f>> subActions) {
        super(provider, actionSet, name, localizedName, XRInputActionType.VECTOR2F, subActions);
    }

    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for(SubAction<Vector2f> entry : subActions) {
                var state = XrActionStateVector2f.calloc(stack)
                        .type(actionType.getStateId());
                getInfo.subactionPath(entry.getPathHandle());
                getInfo.action(handle);
                provider.checkXRError(
                        XR10.xrGetActionStateVector2f(
                                provider.getState().getVrSession().getHandle(),
                                getInfo,
                                state
                        ),
                        "xrGetActionStateFloat"
                );
                entry.update(
                        OpenXRHelper.normalizeXrVector(state.currentState()),
                        state.lastChangeTime(),
                        state.changedSinceLastSync(),
                        state.isActive()
                );
            }
        }
    }


}

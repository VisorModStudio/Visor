package me.phoenixra.atumvr.core.input.action.types.single;

import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateVector2f;
import org.lwjgl.system.MemoryStack;

public class Vec2Action extends OpenXRSingleAction<Vector2fc> {


    public Vec2Action(OpenXRProvider provider,
                      OpenXRActionSet actionSet,
                      String name,
                      String localizedName) {
        super(provider, actionSet, name, localizedName, XRInputActionType.VECTOR2F);
        currentState = new Vector2f();
    }


    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            var state = XrActionStateVector2f.calloc(stack)
                    .type(actionType.getStateId());
            getInfo.action(handle);
            provider.checkXRError(
                    XR10.xrGetActionStateVector2f(
                            provider.getState().getVrSession().getHandle(),
                            getInfo,
                            state
                    ),
                    "xrGetActionStateFloat"
            );
            this.currentState = OpenXRHelper.normalizeXrVector(state.currentState());
            this.changed = state.changedSinceLastSync();
            this.lastChangeTime = state.lastChangeTime();
            this.active = state.isActive();
        }
    }


}

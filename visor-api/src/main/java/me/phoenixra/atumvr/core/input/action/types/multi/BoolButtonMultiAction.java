package me.phoenixra.atumvr.core.input.action.types.multi;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateBoolean;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.List;

public class BoolButtonMultiAction extends OpenXRMultiAction<Boolean> {

    @Getter
    private final List<SubActionBoolButton> subActionsAsButton;

    public BoolButtonMultiAction(OpenXRProvider provider,
                                 OpenXRActionSet actionSet,
                                 String id,
                                 String localizedName,
                                 List<SubActionBoolButton> subActions) {
        super(provider, actionSet, id, localizedName, XRInputActionType.BOOLEAN, subActions);
        subActionsAsButton = Collections.unmodifiableList(subActions);
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

    @Override
    public SubActionBoolButton getHandSubaction(ControllerType type) {
        return (SubActionBoolButton) super.getHandSubaction(type);
    }


    public static class SubActionBoolButton extends SubAction<Boolean> implements VRActionDataButton {


        public SubActionBoolButton(String id, String path, Boolean initialState) {
            super(id, path, initialState);

        }

        @Override
        public SubActionBoolButton putDefaultBindings(@NotNull List<XRInteractionProfile> profiles, @Nullable String source) {
            return (SubActionBoolButton) super.putDefaultBindings(profiles, source);
        }

        @Override
        public SubActionBoolButton putDefaultBindings(@NotNull XRInteractionProfile profile, @Nullable String source) {
            return (SubActionBoolButton) super.putDefaultBindings(profile, source);
        }

        @Override
        public boolean isPressed() {
            return currentState;
        }

        @Override
        public boolean isButtonChanged() {
            return changed;
        }

        @Override
        public long getButtonLastChangeTime() {
            return lastChangeTime;
        }
    }
}

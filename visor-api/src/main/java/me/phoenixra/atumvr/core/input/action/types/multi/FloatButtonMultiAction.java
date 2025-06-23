package me.phoenixra.atumvr.core.input.action.types.multi;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateFloat;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FloatButtonMultiAction extends OpenXRMultiAction<Float> {


    @Getter
    private final float pressThreshold;
    @Getter
    private final float releaseThreshold;


    @Getter
    private final List<SubActionFloatButton> subActionsAsButton;

    public FloatButtonMultiAction(OpenXRProvider provider,
                            OpenXRActionSet actionSet,
                            String id,
                            String localizedName,
                            float pressThreshold,
                            float releaseThreshold,
                            List<SubActionFloatButton> subActions) {
        super(provider, actionSet, id, localizedName, XRInputActionType.FLOAT, subActions);
        this.pressThreshold = pressThreshold;
        this.releaseThreshold = releaseThreshold;
        subActionsAsButton = Collections.unmodifiableList(subActions);

    }

    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update(@Nullable Consumer<String> listener) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            for(SubActionFloatButton entry : subActionsAsButton) {
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
                float v = state.currentState();
                boolean newButtonState;
                if (entry.isPressed()) {
                    // once pressed, only go false when drop below the lower threshold
                    newButtonState = v >= releaseThreshold;
                } else {
                    // only go true once exceed the higher threshold
                    newButtonState = v >= pressThreshold;
                }
                boolean buttonChanged = newButtonState != entry.isPressed();
                long buttonLastChangeTime = entry.getButtonLastChangeTime();;
                if (buttonChanged) {
                    buttonLastChangeTime = System.currentTimeMillis();
                }
                entry.pressed = newButtonState;
                entry.buttonChanged = buttonChanged;
                entry.buttonLastChangeTime = buttonLastChangeTime;


                entry.update(
                        state.currentState(),
                        state.lastChangeTime(),
                        state.changedSinceLastSync(),
                        state.isActive()
                );
                if(listener != null
                        && entry.buttonChanged){
                    listener.accept(entry.getId());
                }
            }
        }
    }

    @Override
    public SubActionFloatButton getHandSubaction(ControllerType type) {
        return (SubActionFloatButton) super.getHandSubaction(type);
    }

    @Getter
    public static class SubActionFloatButton extends SubAction<Float> implements VRActionDataButton {

        protected boolean pressed;
        protected boolean buttonChanged;
        protected long buttonLastChangeTime;

        public SubActionFloatButton(String id, String path, Float initialState) {
            super(id, path, initialState);
        }

        @Override
        public SubActionFloatButton putDefaultBindings(@NotNull List<XRInteractionProfile> profiles,
                                                       @Nullable String source) {
            return (SubActionFloatButton) super.putDefaultBindings(profiles, source);
        }

        @Override
        public SubActionFloatButton putDefaultBindings(@NotNull XRInteractionProfile profile, @Nullable String source) {
            return (SubActionFloatButton) super.putDefaultBindings(profile, source);
        }

    }
}

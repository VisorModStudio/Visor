package me.phoenixra.atumvr.core.input.action.types.single;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateFloat;
import org.lwjgl.system.MemoryStack;

import java.util.function.Consumer;

@Getter
public class FloatButtonAction extends OpenXRSingleAction<Float> implements VRActionDataButton {


    private boolean pressed;
    private boolean buttonChanged;
    private long buttonLastChangeTime;

    private final float pressThreshold;
    private final float releaseThreshold;


    public FloatButtonAction(OpenXRProvider provider,
                       OpenXRActionSet actionSet,
                       String id,
                       String localizedName,
                       float pressThreshold,
                       float releaseThreshold) {
        super(provider, actionSet, id, localizedName, XRInputActionType.FLOAT);
        this.currentState = 0f;
        this.pressThreshold = pressThreshold;
        this.releaseThreshold = releaseThreshold;

    }


    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }
    @Override
    public void update(@Nullable Consumer<String> listener) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            var state = XrActionStateFloat.calloc(stack)
                    .type(actionType.getStateId());
            getInfo.action(handle);
            provider.checkXRError(
                    XR10.xrGetActionStateFloat(
                            provider.getState().getVrSession().getHandle(),
                            getInfo,
                            state
                    ),
                    "xrGetActionStateFloat"
            );

            //BUTTON
            float v = state.currentState();
            boolean newState;
            if (pressed) {
                // once pressed, only go false when drop below the lower threshold
                newState = v >= releaseThreshold;
            } else {
                // only go true once exceed the higher threshold
                newState = v >= pressThreshold;
            }

            this.buttonChanged = newState != pressed;
            this.pressed = newState;

            if (this.buttonChanged) {
                this.buttonLastChangeTime = System.currentTimeMillis();
            }

            //DEFAULT
            this.currentState = state.currentState();
            this.changed = state.changedSinceLastSync();
            this.lastChangeTime = state.lastChangeTime();
            this.active = state.isActive();

            if(listener != null && buttonChanged){
                listener.accept(id);
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }
}

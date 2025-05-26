package me.phoenixra.atumvr.core.input.action.types.single;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateFloat;
import org.lwjgl.system.MemoryStack;

@Getter
public class FloatAction extends OpenXRSingleAction<Float> {

    private final boolean isButton;

    private boolean buttonState;
    private boolean buttonChanged;
    private long buttonLastChangeTime;

    private final float pressThreshold;
    private final float releaseThreshold;

    public FloatAction(OpenXRProvider provider,
                       OpenXRActionSet actionSet,
                       String name,
                       String localizedName) {
        this(provider, actionSet, name, localizedName,  0, 0);
    }

    public FloatAction(OpenXRProvider provider,
                       OpenXRActionSet actionSet,
                       String name,
                       String localizedName,
                       float pressThreshold,
                       float releaseThreshold) {
        super(provider, actionSet, name, localizedName, XRInputActionType.FLOAT);
        this.currentState = 0f;
        this.pressThreshold = pressThreshold;
        this.releaseThreshold = releaseThreshold;
        this.isButton = pressThreshold != 0f || releaseThreshold != 0f;

    }




    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }
    @Override
    public void update() {
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
            if(isButton){
                float v = state.currentState();
                boolean newState;
                if (buttonState) {
                    // once pressed, only go false when drop below the lower threshold
                    newState = v >= releaseThreshold;
                } else {
                    // only go true once exceed the higher threshold
                    newState = v >= pressThreshold;
                }

                this.buttonChanged = newState != buttonState;
                this.buttonState = newState;

                if (this.buttonChanged) {
                    this.buttonLastChangeTime = System.currentTimeMillis();
                }

            }
            this.currentState = state.currentState();
            this.changed = state.changedSinceLastSync();
            this.lastChangeTime = state.lastChangeTime();
            this.active = state.isActive();


        }
    }
}

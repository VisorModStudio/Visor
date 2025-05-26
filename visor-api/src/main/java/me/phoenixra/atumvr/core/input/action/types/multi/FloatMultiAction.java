package me.phoenixra.atumvr.core.input.action.types.multi;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
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

    @Getter
    private final boolean isButton;

    @Getter
    private final float pressThreshold;
    @Getter
    private final float releaseThreshold;

    private final Map<SubAction<Float>, ButtonState> buttonStateMap;


    public FloatMultiAction(OpenXRProvider provider,
                            OpenXRActionSet actionSet,
                            String name,
                            String localizedName,
                            List<SubAction<Float>> subActions) {
        this(provider, actionSet, name, localizedName, 0f,0f, subActions);
    }

    public FloatMultiAction(OpenXRProvider provider,
                            OpenXRActionSet actionSet,
                            String name,
                            String localizedName,
                            float pressThreshold,
                            float releaseThreshold,
                            List<SubAction<Float>> subActions) {
        super(provider, actionSet, name, localizedName, XRInputActionType.FLOAT, subActions);
        this.pressThreshold = pressThreshold;
        this.releaseThreshold = releaseThreshold;
        this.isButton =  pressThreshold != 0f || releaseThreshold != 0f;

        if(!isButton){
            this.buttonStateMap = null;
        }else{
            this.buttonStateMap = new HashMap<>();
            for(var entry : subActions){
                this.buttonStateMap.put(
                        entry,
                        new ButtonState(false, false, 0)
                );
            }
        }

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
                if(isButton){
                    ButtonState buttonState = buttonStateMap.get(entry);
                    float v = state.currentState();
                    boolean newState;
                    if (buttonState.pressed) {
                        // once pressed, only go false when drop below the lower threshold
                        newState = v >= releaseThreshold;
                    } else {
                        // only go true once exceed the higher threshold
                        newState = v >= pressThreshold;
                    }
                    boolean buttonChanged = newState != buttonState.pressed;
                    long buttonLastChangeTime = buttonState.lastChangeTime();;
                    if (buttonChanged) {
                        buttonLastChangeTime = System.currentTimeMillis();
                    }
                    buttonStateMap.put(
                            entry,
                            new ButtonState(
                                    newState,
                                    buttonChanged,
                                    buttonLastChangeTime
                            )
                    );

                }
                entry.update(
                        state.currentState(),
                        state.lastChangeTime(),
                        state.changedSinceLastSync(),
                        state.isActive()
                );
            }
        }
    }


    public ButtonState getButtonState(SubAction<Float> subAction){
        return buttonStateMap.get(subAction);
    }
    public ButtonState getButtonState(ControllerType controllerType){
        return buttonStateMap.get(getHandSubaction(controllerType));
    }

    public record ButtonState(boolean pressed,
                              boolean changed,
                              long lastChangeTime){

    }
}

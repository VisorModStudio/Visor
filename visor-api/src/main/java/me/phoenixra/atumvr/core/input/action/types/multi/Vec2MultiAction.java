package me.phoenixra.atumvr.core.input.action.types.multi;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionStateVector2f;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Vec2MultiAction extends OpenXRMultiAction<Vector2f> {


    @Getter
    private final List<SubActionVec2> subActionsAsVec2;

    public Vec2MultiAction(OpenXRProvider provider,
                           OpenXRActionSet actionSet,
                           String id,
                           String localizedName,
                           List<SubActionVec2> subActions) {
        super(provider, actionSet, id, localizedName, XRInputActionType.VECTOR2F, subActions);
        subActionsAsVec2 = Collections.unmodifiableList(subActions);
    }

    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {

    }

    @Override
    public void update(@Nullable Consumer<String> listener) {
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
                if(listener != null
                        && state.changedSinceLastSync()){
                    listener.accept(entry.getId());
                }
            }
        }
    }


    @Override
    public SubActionVec2 getHandSubaction(ControllerType type) {
        return (SubActionVec2) super.getHandSubaction(type);
    }

    @Getter
    public static class SubActionVec2 extends SubAction<Vector2f> implements VRActionDataVec2 {


        public SubActionVec2(String id, String path, Vector2f initialState) {
            super(id, path, initialState);
        }

        @Override
        public SubActionVec2 putDefaultBindings(@NotNull List<XRInteractionProfile> profiles,
                                                                              @Nullable String source) {
            return (SubActionVec2) super.putDefaultBindings(profiles, source);
        }

        @Override
        public SubActionVec2 putDefaultBindings(@NotNull XRInteractionProfile profile, @Nullable String source) {
            return (SubActionVec2) super.putDefaultBindings(profile, source);
        }

    }

}

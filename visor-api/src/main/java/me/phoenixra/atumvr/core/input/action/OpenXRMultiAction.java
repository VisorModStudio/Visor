package me.phoenixra.atumvr.core.input.action;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.OpenXRInputHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.system.MemoryStack.stackCallocPointer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public abstract class OpenXRMultiAction<T> extends OpenXRAction {

    protected static final XrActionStateGetInfo getInfo = XrActionStateGetInfo.calloc()
            .type(XR10.XR_TYPE_ACTION_STATE_GET_INFO);


    @Getter
    protected final List<SubAction<T>> subActions;


    public OpenXRMultiAction(OpenXRProvider provider,
                             OpenXRActionSet actionSet,
                             String name, String localizedName,
                             XRInputActionType actionType,
                             List<SubAction<T>> subActions) {
        super(provider, actionSet, name, localizedName, actionType);
        this.subActions = Collections.unmodifiableList(subActions);
    }

    protected abstract void onInit(OpenXRActionSet actionSet,
                                   MemoryStack stack);

    public void init(OpenXRActionSet actionSet) {
        OpenXRInputHandler inputHandler = provider.getInputHandler();
        try (var stack = stackPush()) {
            var subactionPaths = stack.callocLong(subActions.size());

            int i = 0;
            for (SubAction<T> entry : subActions) {
                entry.setPathHandle(inputHandler.getPath(entry.pathName));
                subactionPaths.put(i, entry.pathHandle);
                i++;
            }

            XrActionCreateInfo actionCreateInfo = XrActionCreateInfo.calloc(stack).set(
                    XR10.XR_TYPE_ACTION_CREATE_INFO,
                    NULL,
                    memUTF8(this.name),
                    actionType.getId(),
                    subActions.size(),
                    subactionPaths,
                    memUTF8(localizedName)
            );
            PointerBuffer pp = stackCallocPointer(1);

            provider.checkXRError(
                    XR10.xrCreateAction(
                            actionSet.getHandle(),
                            actionCreateInfo,
                            pp
                    ),
                    "xrCreateAction"
            );
            handle = new XrAction(pp.get(), actionSet.getHandle());

            onInit(actionSet, stack);
        }
    }

    public SubAction<T> getHandSubaction(ControllerType type){
        return subActions.get(type.ordinal());
    }

    @Getter
    public static class SubAction<T>{
        protected Map<XRInteractionProfile, String> defaultBindings = new LinkedHashMap<>();

        protected String pathName;
        @Setter
        protected long pathHandle;
        protected T currentState;
        protected long lastChangeTime = 0;

        protected boolean changed = false;
        protected boolean active = false;

        public SubAction(String path, T initialState){
            this.pathName = path;
            this.currentState = initialState;
        }
        public void update(T state,
                           long lastChangeTime,
                           boolean changedSinceLastSync,
                           boolean isActive){
            this.currentState = state;
            this.lastChangeTime = lastChangeTime;
            this.changed = changedSinceLastSync;
            this.active = isActive;
        }

        public SubAction<T> putDefaultBindings(@NotNull XRInteractionProfile profile,
                                               @Nullable String source){
            defaultBindings.put(profile, pathName+"/"+source);
            return this;
        }
        public SubAction<T> putDefaultBindings(@NotNull List<XRInteractionProfile> profiles,
                                               @Nullable String source){
            for(XRInteractionProfile profile : profiles){
                defaultBindings.put(profile, pathName+"/"+source);
            }

            return this;
        }

        @Nullable
        public String getDefaultBindings(XRInteractionProfile profile){
            return defaultBindings.get(profile);
        }
    }
}

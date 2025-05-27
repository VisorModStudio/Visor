package me.phoenixra.atumvr.core.input.action.types.single;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.pose.VRPoseRecord;
import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInputActionType;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.openxr.XR10.XR_NULL_PATH;
import static org.lwjgl.system.MemoryStack.stackCallocPointer;
import static org.lwjgl.system.MemoryUtil.NULL;

public class PoseAction extends OpenXRSingleAction<VRPoseRecord> {



    @Getter
    private XrSpace xrSpace;

    public PoseAction(OpenXRProvider provider,
                      OpenXRActionSet actionSet,
                      String id,
                      String localizedName) {
        super(provider, actionSet, id, localizedName, XRInputActionType.POSE);
        currentState = VRPoseRecord.EMPTY;

    }
    @Override
    protected void onInit(OpenXRActionSet actionSet, MemoryStack stack) {
        XrSession xrSession = provider.getState().getVrSession().getHandle();
        XrActionSpaceCreateInfo action_space_info = XrActionSpaceCreateInfo
                .calloc(stack).set(
                        XR10.XR_TYPE_ACTION_SPACE_CREATE_INFO,
                        NULL,
                        handle,
                        XR_NULL_PATH,
                        OpenXRHelper.getPoseIdentity(stack)
                );
        PointerBuffer pp = stackCallocPointer(1);
        provider.checkXRError(
                XR10.xrCreateActionSpace(
                        xrSession,
                        action_space_info, pp
                ),
                "xrCreateActionSpace"
        );
        xrSpace = new XrSpace(pp.get(0), xrSession);
    }


    @Override
    public void update() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            var state = XrActionStatePose.calloc(stack)
                    .type(actionType.getStateId());
            getInfo.action(handle);
            provider.checkXRError(
                    XR10.xrGetActionStatePose(
                            provider.getState().getVrSession().getHandle(),
                            getInfo,
                            state
                    ),
                    "xrGetActionStateFloat"
            );
            this.changed = true;
            this.lastChangeTime = System.nanoTime();
            this.active = state.isActive();

            var loc = OpenXRHelper.xrLocationFromSpace(
                    provider, xrSpace, stack
            );

            currentState = loc == null
                    ? VRPoseRecord.EMPTY
                    :
                    new VRPoseRecord(
                            OpenXRHelper.normalizeXrPose(loc.pose()),
                            OpenXRHelper.normalizeXrQuaternion(loc.pose().orientation()),
                            OpenXRHelper.normalizeXrVector(loc.pose().position$())
                    );
        }
    }
}

package me.phoenixra.atumvr.core.input.device;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.VRDeviceHMD;
import me.phoenixra.atumvr.api.misc.pose.VRPose;
import me.phoenixra.atumvr.api.misc.pose.VRPoseMutable;
import me.phoenixra.atumvr.core.OpenXRHelper;
import me.phoenixra.atumvr.core.OpenXRProvider;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

public class OpenXRDeviceHMD extends OpenXRDevice implements VRDeviceHMD {

    private final VRPoseMutable eyeLeftPose = new VRPoseMutable();

    private final VRPoseMutable eyeRightPose = new VRPoseMutable();

    private final XrSpace space;

    public OpenXRDeviceHMD(OpenXRProvider provider) {
        super(provider, ID);
        space = provider.getState().getVrSession().getXrViewSpace();
    }



    @Override
    public void update() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            XrSpaceLocation loc = OpenXRHelper.xrLocationFromSpace(
                    provider,
                    space,
                    stack
            );

            active = loc != null;

            if (active) {
                pose.update(
                        OpenXRHelper.normalizeXrPose(loc.pose()),
                        OpenXRHelper.normalizeXrQuaternion(loc.pose().orientation()),
                        OpenXRHelper.normalizeXrVector(loc.pose().position$())
                );
            }
        }

        XrPosef eyePose = getXrView(EyeType.LEFT).pose();
        eyeLeftPose.update(
                OpenXRHelper.normalizeXrPose(eyePose),
                OpenXRHelper.normalizeXrQuaternion(eyePose.orientation()),
                OpenXRHelper.normalizeXrVector(eyePose.position$())
        );

        eyePose = getXrView(EyeType.RIGHT).pose();
        eyeRightPose.update(
                OpenXRHelper.normalizeXrPose(eyePose),
                OpenXRHelper.normalizeXrQuaternion(eyePose.orientation()),
                OpenXRHelper.normalizeXrVector(eyePose.position$())
        );
    }

    @Override
    public @NotNull VRPose getEyePose(@NotNull EyeType eyeType) {
        if(eyeType == EyeType.LEFT){
            return eyeLeftPose;
        }else{
            return eyeRightPose;
        }
    }

    public XrView getXrView(EyeType eyeType){
        return provider.getState().getVrSwapChain()
                .getXrViewBuffer().get(eyeType.getIndex());
    }
}

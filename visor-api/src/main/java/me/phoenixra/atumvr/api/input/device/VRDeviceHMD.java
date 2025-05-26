package me.phoenixra.atumvr.api.input.device;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.misc.pose.VRPose;
import org.jetbrains.annotations.NotNull;

public interface VRDeviceHMD extends VRDevice{
    String ID = "hmd";

    @NotNull
    VRPose getEyePose(@NotNull EyeType eyeType);

}

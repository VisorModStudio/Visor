package me.phoenixra.atumvr.api.input.device;

import me.phoenixra.atumvr.api.misc.pose.VRPose;
import org.jetbrains.annotations.NotNull;

public interface VRDevice {

    @NotNull
    String getId();

    boolean isActive();


    @NotNull
    VRPose getPose();
}

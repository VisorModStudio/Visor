package me.phoenixra.atumvr.core.input.device;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.device.VRDevice;
import me.phoenixra.atumvr.api.misc.pose.VRPose;
import me.phoenixra.atumvr.api.misc.pose.VRPoseMutable;
import me.phoenixra.atumvr.core.OpenXRProvider;
import org.jetbrains.annotations.NotNull;


public abstract class OpenXRDevice implements VRDevice {
    @Getter
    protected final OpenXRProvider provider;
    @Getter
    private final String id;


    protected final VRPoseMutable pose = new VRPoseMutable();

    @Getter
    protected boolean active;


    public OpenXRDevice(OpenXRProvider provider, String id){
        this.provider = provider;
        this.id = id;
    }

    public abstract void update();


    @Override
    public @NotNull VRPose getPose() {
        return pose;
    }
}

package me.phoenixra.atumvr.api.input;

import me.phoenixra.atumvr.api.VRProvider;
import me.phoenixra.atumvr.api.input.action.VRActionSet;
import me.phoenixra.atumvr.api.input.device.VRDevice;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public interface VRInputHandler {

    void init();

    void update();

    void destroy();



    Collection<? extends VRActionSet> getActionSets();


    void registerDevice(VRDevice device);

    VRDevice getDevice(String id);

    default <T extends VRDevice> T getDevice(String id, Class<T> clazz){
        return (T) getDevice(id);
    }

    Collection<? extends VRDevice> getDevices();


    @NotNull
    VRProvider getVrProvider();


}

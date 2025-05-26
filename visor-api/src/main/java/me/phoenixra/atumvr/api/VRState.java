package me.phoenixra.atumvr.api;

import org.jetbrains.annotations.NotNull;

public interface VRState {



    boolean isInitialized();


    boolean isRunning();

    //If VR session is active
    boolean isActive();

    //If Focused on this session
    boolean isFocused();



    @NotNull
    VRProvider getVrProvider();

}

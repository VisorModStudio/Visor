package me.phoenixra.atumvr.api;


import me.phoenixra.atumvr.api.input.VRInputHandler;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.rendering.VRRenderer;
import org.jetbrains.annotations.NotNull;


public interface VRProvider {

    void initializeVR() throws Throwable;

    //has to run before preRender and even if rendering currently inactive
    void syncState();

    void preRender(@NotNull IRenderContext context);
    void render(@NotNull IRenderContext context);
    void postRender(@NotNull IRenderContext context);


    void destroy();



    @NotNull
    VRState getState();

    @NotNull
    VRInputHandler getInputHandler();

    @NotNull
    VRRenderer getRenderer();



    @NotNull String getAppName();

    @NotNull
    VRLogger getLogger();


}

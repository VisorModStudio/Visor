package me.phoenixra.visor.api;

import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access point for client-side state values
 */
public interface VisorClientState {


    /**
     * Get User’s selected VR play mode.
     *
     * @return the current {@link VRPlayMode}
     */
    @NotNull
    VRPlayMode playMode();


    /**
     * Get VR session state
     *
     * @return the current {@link VRStateMode}
     */
    @NotNull
    VRStateMode stateMode();


    /**
     * Get Render Phase
     *
     * @return the current {@link RenderPhase}
     */
    @NotNull
    RenderPhase renderPhase();

    /**
     * Get VR Display
     *
     * @return the current {@link VRDisplay} or null
     */
    @Nullable("Not rendering VR display")
    VRDisplay renderingDisplay();
}

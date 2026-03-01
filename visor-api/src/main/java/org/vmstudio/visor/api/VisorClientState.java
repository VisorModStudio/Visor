package org.vmstudio.visor.api;

import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access point for client-side state values
 */
public interface VisorClientState {


    /**
     * Get VR play mode.
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
     * Get VR Camera type that is currently rendered
     *
     * @return the current {@link VRCameraType} or null
     */
    @Nullable
    VRCameraType renderCameraType();
}

package org.vmstudio.visor.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access point for client-side state values
 */
@Environment(EnvType.CLIENT)
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
     * Get VR render pass that is currently rendered
     *
     * @return the current {@link VRRenderPass} or null
     */
    @Nullable
    VRRenderPass renderPass();



    @Environment(EnvType.CLIENT)
    final class Empty implements VisorClientState {

        public static final Empty INSTANCE = new Empty();

        private Empty() {}

        @Override public @NotNull VRPlayMode playMode()      { return VRPlayMode.DISABLED; }
        @Override public @NotNull VRStateMode stateMode()    { return VRStateMode.OFF; }
        @Override public @NotNull RenderPhase renderPhase()  { return RenderPhase.VANILLA; }
        @Override public @Nullable VRRenderPass renderPass() { return null; }
    }
}

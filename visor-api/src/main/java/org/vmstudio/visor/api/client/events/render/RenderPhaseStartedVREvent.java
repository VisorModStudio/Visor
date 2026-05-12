package org.vmstudio.visor.api.client.events.render;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;


/**
 * Fired when the renderer transitions into a new {@link RenderPhase}.
 *
 * <p>
 *     Use this to initialize per-pass state,
 *     for example clear projection caches, swap shaders, reset uniforms.
 * </p>
 */
@Getter
public class RenderPhaseStartedVREvent extends VREvent {

    @NotNull private final RenderPhase previousPhase;
    @NotNull private final RenderPhase newPhase;
    @NotNull private final VRRenderPass renderPass;

    public RenderPhaseStartedVREvent(@NotNull RenderPhase previousPhase,
                                     @NotNull RenderPhase newPhase,
                                     @NotNull VRRenderPass renderPass) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.previousPhase = previousPhase;
        this.newPhase = newPhase;
        this.renderPass = renderPass;
    }

    public boolean enteredVRWorld()  {
        return previousPhase != RenderPhase.VR_WORLD
                && newPhase == RenderPhase.VR_WORLD;
    }
    public boolean enteredVRGui()    {
        return previousPhase != RenderPhase.VR_GUI
                && newPhase == RenderPhase.VR_GUI;
    }
    public boolean enteredVRMirror() {
        return previousPhase != RenderPhase.VR_MIRROR
                && newPhase == RenderPhase.VR_MIRROR;
    }
    public boolean enteredVanilla()  {
        return previousPhase != RenderPhase.VANILLA
                && newPhase == RenderPhase.VANILLA;
    }
}
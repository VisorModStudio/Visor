package org.vmstudio.visor.api.client.events.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;

/**
 * Fired at each {@link RenderPipelineStage} during a VR render pipeline,
 * after Visor's core drawing for that stage has finished.
 *
 */
@Getter
public class RenderPipelineStageVREvent extends VREvent {
    @NotNull private final RenderPipelineStage stage;
    @NotNull private final RenderPhase renderPhase;
    @NotNull private final VRRenderPass renderPass;
    @NotNull private final PoseStack poseStack;
    private final float partialTicks;

    public RenderPipelineStageVREvent(@NotNull RenderPipelineStage stage,
                                      @NotNull RenderPhase renderPhase,
                                      @NotNull VRRenderPass renderPass,
                                      @NotNull PoseStack poseStack,
                                      float partialTicks) {
        super(VisorAPI.addonManager().getCoreAddon());
        this.stage = stage;
        this.renderPhase = renderPhase;
        this.renderPass = renderPass;
        this.poseStack = poseStack;
        this.partialTicks = partialTicks;
    }
}

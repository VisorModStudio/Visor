package org.vmstudio.visor.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;

/**
 * A callback that is used
 * {@link ModLoader#addToRenderPipeline(RenderPipelineStage, RenderPipelineCallback) here}
 *
 */
@FunctionalInterface
public interface RenderPipelineCallback {

    /**
     * Called by the mod loader at the registered pipeline stage.
     *
     * @param poseStack    the current pose stack from the render event
     * @param partialTicks the partial tick
     */
    void render(@NotNull PoseStack poseStack, float partialTicks);
}
package org.vmstudio.visor.extensions.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public interface PlayerRendererExtension {

    /**
     * Runs LivingEntityRenderer#render on the player renderer, bypassing PlayerRenderer's own
     * dispatch. Our PlayerRenderer subclasses must use this instead of plain super.render(...);
     * see PlayerRenderMixins.PlayerRendererMixin for why.
     */
    void visor$renderVanilla(PlayerRenderState renderState, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight);

}

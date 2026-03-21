package org.vmstudio.visor.extensions.client.render;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ItemInHandRendererExtension {
    void visor$renderMap(PoseStack poseStack,
                         MultiBufferSource bufferSource,
                         int pCombinedLight,
                         ItemStack itemStack);
    float visor$getEquipProgress(InteractionHand hand, float partialTicks);
}

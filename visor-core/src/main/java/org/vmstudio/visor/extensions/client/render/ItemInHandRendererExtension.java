package org.vmstudio.visor.extensions.client.render;


import com.mojang.blaze3d.vertex.PoseStack;
//? if >=26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
//? if >=26.3 {
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
//?}

public interface ItemInHandRendererExtension {
    void visor$renderMap(PoseStack poseStack,
                         //? if >=26.2 {
                         SubmitNodeCollector bufferSource,
                         //?} else {
                         /*MultiBufferSource bufferSource,
                         *///?}
                         int pCombinedLight,
                         ItemStack itemStack);
    float visor$getEquipProgress(InteractionHand hand, float partialTicks);

    //? if >=26.3 {
    void visor$renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
                          PoseStack poseStack, SubmitNodeCollector collector, int lightCoords);
    //?}
}

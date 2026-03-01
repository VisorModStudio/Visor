package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.common.HandType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VRPlayerRendererArms extends PlayerRenderer {

    public VRPlayerRendererArms(EntityRendererProvider.Context context,
                                boolean playerSlim) {
        super(context, playerSlim);
    }

    @Override
    public void renderRightHand(PoseStack poseStack,
                                MultiBufferSource bufferSource,
                                int combinedLight,
                                AbstractClientPlayer player) {
        this.renderHand(
                poseStack, bufferSource,
                combinedLight, player,
                (this.model).rightArm, (this.model).rightSleeve,
                HandType.MAIN
        );
    }

    @Override
    public void renderLeftHand(PoseStack poseStack,
                               MultiBufferSource bufferSource,
                               int combinedLight,
                               AbstractClientPlayer player) {
        this.renderHand(
                poseStack, bufferSource,
                combinedLight, player,
                (this.model).leftArm, (this.model).leftSleeve,
                HandType.OFFHAND
        );
    }

    private void renderHand(PoseStack poseStack,
                            MultiBufferSource bufferSource,
                            int combinedLightIn,
                            AbstractClientPlayer playerIn,
                            ModelPart rendererArmIn,
                            ModelPart rendererArmwearIn,
                            HandType hand) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        this.setModelProperties(playerIn);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        rendererArmIn.xRot = 0.0F;
        playermodel.leftSleeve.copyFrom(playermodel.leftArm);
        playermodel.rightSleeve.copyFrom(playermodel.rightArm);
        float fade = getItemFade(
                (LocalPlayer) playerIn,
                hand,
                ItemStack.EMPTY
        );
        rendererArmIn.render(
                poseStack,
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                playerIn.getSkinTextureLocation()
                        )
                ),
                combinedLightIn,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, fade
        );
        rendererArmwearIn.xRot = 0.0F;
        rendererArmwearIn.render(
                poseStack,
                bufferSource.getBuffer(
                        RenderType.entityTranslucent(
                                playerIn.getSkinTextureLocation()
                        )
                ),
                combinedLightIn,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, fade
        );
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }


    private float getItemFade(LocalPlayer player,
                              HandType hand,
                              ItemStack itemStack) {
        float fade = player.getAttackStrengthScale(0.0F) * 0.75F + 0.25F;

        if (player.isShiftKeyDown()) {
            fade = 0.75F;
        }


        if (itemStack != ItemStack.EMPTY) {
            if (player.isBlocking()
                    && player.getUseItem() != itemStack) {
                fade -= 0.25F;
            }

            if (itemStack.getItem() == Items.SHIELD
                    && !player.isBlocking()) {
                fade -= 0.25F;
            }
        }

        if ((double) fade < 0.1) {
            fade = 0.1F;
        }

        if (fade > 1F) {
            fade = 1;
        }

        return fade;
    }
}

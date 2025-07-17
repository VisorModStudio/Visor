package me.phoenixra.visor.core.mixin.client.renderer.entity.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.input.HandAction;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.entity.EntityRenderDispatcherVRModified;
import me.phoenixra.visor.core.client.mcmodified.render.ItemInHandRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.player.VRPlayerRendererArms;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererMixin implements ItemInHandRendererModified {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float offHandHeight;


    @Unique
    private HandAction visor$swingType = HandAction.ATTACK;

    @Shadow
    public abstract void renderItem(LivingEntity livingEntity,
                                    ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext,
                                    boolean bl,
                                    PoseStack poseStack,
                                    MultiBufferSource multiBufferSource,
                                    int i);

    @Shadow
    protected abstract void renderMap(PoseStack pMatrixStack,
                                      MultiBufferSource pBuffer,
                                      int pCombinedLight,
                                      ItemStack pStack);

    @Shadow
    protected abstract void renderPlayerArm(PoseStack pMatrixStack,
                                            MultiBufferSource pBuffer,
                                            int pCombinedLight,
                                            float pEquippedProgress,
                                            float pSwingProgress,
                                            HumanoidArm humanoidarm);


    @Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
    public void visor$overrideArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g,
                                 HumanoidArm humanoidArm, CallbackInfo ci
    ) {
        if (VisorState.getState().isNotActive()) {
            return;
        }
        visor$renderVrArm(
                poseStack,
                multiBufferSource,
                i, f, g,
                humanoidArm
        );
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    public void visor$overrideArmItem(AbstractClientPlayer abstractClientPlayer, float f, float g,
                                     InteractionHand interactionHand, float h, ItemStack itemStack, float i,
                                     PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci
    ) {
        if (VisorState.getState().isNotActive()) {
            return;
        }
        this.visor$renderVrArmWithItem(
                abstractClientPlayer,
                f, g,
                interactionHand,
                h,
                itemStack,
                i,
                poseStack,
                multiBufferSource,
                j
        );
        ci.cancel();
    }

    @Unique
    private void visor$renderVrArmWithItem(AbstractClientPlayer pPlayer,
                                          float pPartialTicks,
                                          float pPitch,
                                          InteractionHand pHand,
                                          float pSwingProgress,
                                          ItemStack itemStack,
                                          float pEquippedProgress,
                                          PoseStack poseStack,
                                          MultiBufferSource pBuffer,
                                          int pCombinedLight
    ) {
        boolean mainHand = pHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = mainHand
                ? pPlayer.getMainArm()
                : pPlayer.getMainArm().getOpposite();
        pEquippedProgress = this.visor$getEquipProgress(pHand, pPartialTicks);


        boolean renderArm =
                VRRenderState.getCurrentVRDisplay() != VRDisplay.THIRD_PERSON
                || (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                        && VRClientSettings.isMixedRealityRenderHands());

        poseStack.pushPose();
        if (renderArm && !pPlayer.isInvisible()) {
            this.renderPlayerArm(
                    poseStack,
                    pBuffer,
                    pCombinedLight,
                    pEquippedProgress,
                    pSwingProgress,
                    humanoidarm
            );
        }

        if (itemStack.isEmpty()) {
            poseStack.popPose();
            return;
        }

        if (pPlayer.swingingArm == pHand) {
            this.visor$applySwingPose(
                    poseStack,
                    humanoidarm,
                    pSwingProgress
            );
        }


        ClientContext.handRenderer.applyItemHandPose(
                pPlayer,
                mainHand ? ControllerHand.MAIN : ControllerHand.OFFHAND,
                itemStack,
                poseStack,
                pEquippedProgress,
                pPartialTicks
        );


        if (itemStack.getItem() instanceof MapItem) {
            RenderSystem.disableCull();
            this.renderMap(
                    poseStack,
                    pBuffer,
                    pCombinedLight,
                    itemStack
            );
        } else {
            this.renderItem(
                    pPlayer,
                    itemStack,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    false,
                    poseStack,
                    pBuffer,
                    pCombinedLight
            );
        }

        poseStack.popPose();
    }

    @Unique
    private void visor$renderVrArm(PoseStack poseStack,
                                  MultiBufferSource multiBufferSource,
                                  int i, float f, float swingProgress,
                                  HumanoidArm humanoidArm
    ) {
        boolean mainHand = humanoidArm != HumanoidArm.LEFT;
        float handFactor = mainHand ? 1.0F : -1.0F;
        AbstractClientPlayer player = this.minecraft.player;
        RenderSystem.setShaderTexture(
                0,
                player.getSkinTextureLocation()
        );
        VRPlayerRendererArms rendererArms = ((EntityRenderDispatcherVRModified) entityRenderDispatcher)
                .visor$getArmSkinMap()
                .get(player.getModelName());

        poseStack.pushPose();

        boolean swingingArm = (player.swingingArm == InteractionHand.OFF_HAND
                && !mainHand)
                || (player.swingingArm == InteractionHand.MAIN_HAND
                && mainHand);
        if (swingingArm) {
            this.visor$applySwingPose(poseStack, humanoidArm, swingProgress);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = player.getModelName()
                .equals("slim");


        poseStack.translate(
                (slim ? -0.34375F : -0.375F) * handFactor,
                0.0F,
                slim ? 0.78125F : 0.75F
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        if (mainHand) {
            rendererArms.renderRightHand(
                    poseStack, multiBufferSource,
                    i, player
            );
        } else {
            rendererArms.renderLeftHand(
                    poseStack, multiBufferSource,
                    i, player
            );
        }
        poseStack.popPose();
    }

    @Unique
    private void visor$applySwingPose(PoseStack matrixStackIn,
                                     HumanoidArm hand,
                                     float swingProgress) {
        if (swingProgress == 0.0F) {
            return;
        }
        switch (this.visor$swingType) {
            case ATTACK ->{
                float swingAngle;
                if ((double) swingProgress > 0.5D) {
                    swingAngle = Mth.sin(
                            (float) ((double) swingProgress * Math.PI + Math.PI)
                    );
                } else {
                    swingAngle = Mth.sin(
                            (float) ((double) (swingProgress * 3.0F) * Math.PI)
                    );
                }

                matrixStackIn.translate(
                        0.0D, 0.0D, 0.2F
                );
                matrixStackIn.mulPose(
                        Axis.XP.rotationDegrees(
                                swingAngle * 30.0F
                        )
                );
                matrixStackIn.translate(
                        0.0D, 0.0D, -0.2F
                );
            }
            case INTERACT -> {
                float swingAngle;

                if ((double) swingProgress > 0.5D) {
                    swingAngle = Mth.sin(
                            (float) ((double) swingProgress * Math.PI + Math.PI)
                    );
                } else {
                    swingAngle = Mth.sin(
                            (float) ((double) (swingProgress * 3.0F) * Math.PI)
                    );
                }

                matrixStackIn.mulPose(
                        Axis.ZP.rotationDegrees(
                                (float) (hand == HumanoidArm.RIGHT ? -1 : 1) * swingAngle * 45.0F
                        )
                );
            }
            case USE -> {
                float swingOffset;

                if ((double) swingProgress > 0.25D) {
                    swingOffset = Mth.sin((float) ((double) (swingProgress / 2.0F) * Math.PI + Math.PI));
                } else {
                    swingOffset = Mth.sin(
                            (float) ((double) (swingProgress * 2.0F) * Math.PI)
                    );
                }
                matrixStackIn.translate(
                        0.0D,
                        0.0D,
                        -(1.0F + swingOffset) * 0.1F
                );
            }
        }
    }

    @Unique
    private float visor$getEquipProgress(InteractionHand hand, float partialTicks) {
        return hand == InteractionHand.MAIN_HAND
                ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks)
                : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
    }
    @Override
    @Unique
    public void visor$setSwingType(HandAction interact) {
        this.visor$swingType = interact;
    }
}

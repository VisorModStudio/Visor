package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.ArmPoseClamp;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin extends RenderLayer {

    public ItemInHandLayerMixin(RenderLayerParent renderer) {
        super(renderer);
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("STORE"), ordinal = 0)
    private boolean visor$isRightMainHand(boolean isRightMainHand, @Local(argsOnly = true) LivingEntity entity) {
        if (this.getParentModel() instanceof PlayerModel<?>) {
            var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
            if (vrPlayer != null) {
                return !vrPlayer.isLeftHanded();
            }
            return true;
        } else {
            return isRightMainHand;
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$firstPersonItemScale(
            CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) PoseStack poseStack)
    {
        if (VRRenderState.isSelfModelRender(entity)) {
            var itemScale = ClientContext.localPlayer.getBodyType().getRenderer().getModelItemScale();
            poseStack.translate(0.0F, 0.65F, 0.0F);
            poseStack.scale(itemScale.x(), itemScale.y(), itemScale.z());
            poseStack.translate(0.0F, -0.65F, 0.0F);
        }
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$thirdPersonWristResidual(
            CallbackInfo ci,
            @Local(argsOnly = true) LivingEntity entity,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        if (VRRenderState.isSelfModelRender(entity)) return;
        if (!(entity instanceof AbstractClientPlayer player)) return;

        VRClientPlayer vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer == null) return;

        HandType handType = (arm == player.getMainArm()) ? HandType.MAIN : HandType.OFFHAND;
        var poseRender = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        VRPose handPose = poseRender.getBody().getHand(handType).getPose();

        ArmPoseClamp.ArmFrame frame = ArmPoseClamp.solveArmFrame(
                player.getUUID(), handPose, poseRender.getBodyYaw(), arm == HumanoidArm.LEFT);
        poseStack.mulPose(frame.wristResidual);
    }

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void visor$applyItemHandPose(
            CallbackInfo ci,
            @Local(argsOnly = true) LivingEntity entity,
            @Local(argsOnly = true) ItemStack itemStack,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        if (ClientContext.handRenderer == null) return;
        if (!(entity instanceof AbstractClientPlayer player)) return;

        HandType hand = (arm == player.getMainArm()) ? HandType.MAIN : HandType.OFFHAND;
        InteractionHand mcHand = hand == HandType.MAIN
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        float equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(mcHand, MC.getFrameTime());

        ClientContext.handRenderer.applyItemHandPose(
                player, hand, itemStack, poseStack, equipProgress, MC.getFrameTime()
        );
    }
}
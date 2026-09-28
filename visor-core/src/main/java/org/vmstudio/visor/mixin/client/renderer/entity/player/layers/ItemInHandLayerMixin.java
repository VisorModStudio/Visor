package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;
//?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;
*///?} else {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.PlayerModel;
*///?}

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin extends RenderLayer {

    public ItemInHandLayerMixin(RenderLayerParent renderer) {
        super(renderer);
    }

    //? if <1.21.2 {
    /*@ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;"))
    private HumanoidArm visor$vrMainArm(HumanoidArm vanillaArm, @Local(argsOnly = true) LivingEntity entity) {
        if (!(this.getParentModel() instanceof PlayerModel<?>)) {
            return vanillaArm;
        }
        var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
        boolean leftHanded = vrPlayer != null && vrPlayer.isLeftHanded();
        return leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }
    *///?}

    //? if >=1.21.9 {
    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$scaleItemWithModelArms(
            CallbackInfo ci, @Local(argsOnly = true) ArmedEntityRenderState state, @Local(argsOnly = true) PoseStack poseStack)
    {
        visor$scaleItem(VRPlayerRenderState.playerOf(state), poseStack);
    }

    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void visor$applyItemHandPose(
            CallbackInfo ci,
            @Local(argsOnly = true) ArmedEntityRenderState state,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        if (player == null) {
            return;
        }
        visor$applyHandPose(player, VRPlayerRenderState.heldItemForArm(player, arm), arm, poseStack);
    }
    //?} elif >=1.21.4 {
    /*@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$scaleItemWithModelArms(
            CallbackInfo ci, @Local(argsOnly = true) ArmedEntityRenderState state, @Local(argsOnly = true) PoseStack poseStack)
    {
        visor$scaleItem(VRPlayerRenderState.playerOf(state), poseStack);
    }

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void visor$applyItemHandPose(
            CallbackInfo ci,
            @Local(argsOnly = true) ArmedEntityRenderState state,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        if (player == null) {
            return;
        }
        visor$applyHandPose(player, VRPlayerRenderState.heldItemForArm(player, arm), arm, poseStack);
    }
    *///?} elif >=1.21.2 {
    /*@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$scaleItemWithModelArms(
            CallbackInfo ci, @Local(argsOnly = true) LivingEntityRenderState state, @Local(argsOnly = true) PoseStack poseStack)
    {
        visor$scaleItem(VRPlayerRenderState.playerOf(state), poseStack);
    }

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void visor$applyItemHandPose(
            CallbackInfo ci,
            @Local(argsOnly = true) LivingEntityRenderState state,
            @Local(argsOnly = true) ItemStack itemStack,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        visor$applyHandPose(VRPlayerRenderState.playerOf(state), itemStack, arm, poseStack);
    }
    *///?} else {
    /*@Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$scaleItemWithModelArms(
            CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) PoseStack poseStack)
    {
        visor$scaleItem(entity, poseStack);
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
        visor$applyHandPose(entity, itemStack, arm, poseStack);
    }
    *///?}

    @Unique
    private static void visor$scaleItem(@Nullable LivingEntity entity, PoseStack poseStack) {
        if (entity == null || !VRRenderState.isSelfModelRender(entity)) {
            return;
        }
        var itemScale = ClientContext.localPlayer.getBodyType().getRenderer().getModelItemScale();
        final float gripY = 0.65F;
        poseStack.translate(0.0F, gripY, 0.0F);
        poseStack.scale(itemScale.x(), itemScale.y(), itemScale.z());
        poseStack.translate(0.0F, -gripY, 0.0F);
    }

    @Unique
    private static void visor$applyHandPose(
            @Nullable LivingEntity entity, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack)
    {
        if (ClientContext.handRenderer == null) return;
        if (!(entity instanceof AbstractClientPlayer player)) return;

        HandType hand = (arm == player.getMainArm()) ? HandType.MAIN : HandType.OFFHAND;
        InteractionHand mcHand = hand == HandType.MAIN
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        float equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(mcHand, McRenderUtils.partialTick());

        //@TODO rework this since the change is globally applied and might be a problem for addons to work with
        if (!VRRenderState.isSelfModelRender(entity)) {
            VRClientPlayer vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
            if (vrPlayer != null) {
                VRPose handPose = vrPlayer.getPoseData(PlayerPoseType.RENDER)
                        .getBody().getHand(hand).getPose();
                Vector3f aim = new Vector3f(handPose.getDirection());
                if (aim.lengthSquared() > 1.0e-8f) {
                    aim.normalize();

                    Vector3f refUp = new Vector3f(VRMathUtils.UP_VECTOR);
                    refUp.sub(new Vector3f(aim).mul(refUp.dot(aim)));
                    if (refUp.lengthSquared() < 1.0e-6f) {
                        refUp.set(VRMathUtils.FORWARD_VECTOR);
                        refUp.sub(new Vector3f(aim).mul(refUp.dot(aim)));
                    }
                    refUp.normalize();

                    // Controller's actual up, projected perpendicular to aim.
                    Vector3f ctrlUp = handPose.transformDirection(VRMathUtils.UP_VECTOR);
                    ctrlUp.sub(new Vector3f(aim).mul(ctrlUp.dot(aim)));

                    if (ctrlUp.lengthSquared() > 1.0e-8f) {
                        ctrlUp.normalize();

                        // Signed angle refUp -> ctrlUp about the aim axis.
                        float cos = Mth.clamp(refUp.dot(ctrlUp), -1.0f, 1.0f);
                        float sin = new Vector3f(refUp).cross(ctrlUp).dot(aim);
                        float roll = (float) Math.atan2(sin, cos);

                        poseStack.mulPose(Axis.ZP.rotation(-roll));
                    }
                }
            }
        }

        ClientContext.handRenderer.applyItemHandPose(
                player, hand, itemStack, poseStack, equipProgress, McRenderUtils.partialTick()
        );
    }
}

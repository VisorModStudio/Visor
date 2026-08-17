package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    // fully qualified so it can never drift onto a bridge overload of the same name
    private static final String RENDER_ARM_WITH_ITEM =
            "renderArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;"
                    + "Lnet/minecraft/client/renderer/item/ItemStackRenderState;"
                    + "Lnet/minecraft/world/entity/HumanoidArm;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = RENDER_ARM_WITH_ITEM,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER))
    private void visor$firstPersonItemScale(
            CallbackInfo ci,
            @Local(argsOnly = true) ArmedEntityRenderState renderState,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        if (((EntityRenderStateExtension) renderState).visor$isSelfModelRender()) {
            var itemScale = ClientContext.localPlayer.getBodyType().getRenderer().getModelItemScale();
            poseStack.translate(0.0F, 0.65F, 0.0F);
            poseStack.scale(itemScale.x(), itemScale.y(), itemScale.z());
            poseStack.translate(0.0F, -0.65F, 0.0F);
        }
    }

    @Inject(method = RENDER_ARM_WITH_ITEM,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void visor$applyItemHandPose(
            CallbackInfo ci,
            @Local(argsOnly = true) ArmedEntityRenderState renderState,
            @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) PoseStack poseStack)
    {
        if (ClientContext.handRenderer == null) return;

        EntityRenderStateExtension ext = (EntityRenderStateExtension) renderState;
        VRClientPlayer vrPlayer = ext.visor$getVRPlayer();
        if (vrPlayer == null) return;
        if (!(vrPlayer.getMcPlayer() instanceof AbstractClientPlayer player)) return;

        // renderState.mainArm already carries the VR handedness, so hand and stack agree
        HandType hand = (arm == renderState.mainArm) ? HandType.MAIN : HandType.OFFHAND;
        InteractionHand mcHand = hand == HandType.MAIN
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        ItemStack itemStack = player.getItemInHand(mcHand);

        float partialTicks = MC.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(mcHand, partialTicks);

        //@TODO rework this since the change is globally applied and might be a problem for addons to work with
        if (!ext.visor$isSelfModelRender()) {
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
                Vector3f ctrlUp = handPose.getCustomVector(VRMathUtils.UP_VECTOR);
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

        ClientContext.handRenderer.applyItemHandPose(
                player, hand, itemStack, poseStack, equipProgress, partialTicks
        );
    }
}

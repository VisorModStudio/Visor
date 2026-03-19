package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.utils.ModelUtils;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    @Unique
    private final Vector3f visor$tempV = new Vector3f();

    @Unique
    private final Matrix3f visor$bodyRot = new Matrix3f();

    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    // DEBUG CAPE
    /*
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/PlayerSkin;capeTexture()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation visor$whiteCape(PlayerSkin skin, Operation<ResourceLocation> original) {
        ResourceLocation capeTexture = original.call(skin);
        if (capeTexture == null) {
            capeTexture = RenderHelper.WHITE_TEXTURE;
        }
        return capeTexture;
    }
    */

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$modifyOffset(
        PoseStack poseStack, float x, float y, float z, Operation<Void> original,
        @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true, ordinal = 2) float partialTick,
        @Share("xRot") LocalFloatRef xRotation, @Share("yRot") LocalFloatRef yRotation)
    {
        // don't care about interpolation here, only needs the scales which aren't interpolated
        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        // only do this if it's a vr player
        if (vrPlayer != null) {
            this.visor$bodyRot.rotationZYX(getParentModel().body.zRot, -getParentModel().body.yRot,
                -getParentModel().body.xRot);

            // attach the cape to the body
            this.visor$bodyRot.transform(VRMathUtils.UP_VECTOR, this.visor$tempV);
            xRotation.set((float) Math.atan2(this.visor$tempV.y, this.visor$tempV.z) - Mth.HALF_PI);

            // make sure it doesn't go below -PI
            xRotation.set(xRotation.get() < -Mth.PI ? xRotation.get() + Mth.TWO_PI : xRotation.get());

            this.visor$bodyRot.transform(VRMathUtils.LEFT_VECTOR, this.visor$tempV);
            yRotation.set((float) -Math.atan2(this.visor$tempV.x, this.visor$tempV.y) + Mth.HALF_PI);

            // transform offset to be body relative
            this.visor$tempV.set(0F, 0F, 2F - 0.5F * (getParentModel().body.xRot / Mth.HALF_PI));
            if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
                // vanilla cape offset with armor
                this.visor$tempV.add(0F, -0.85F, 1.1F);
            }
            this.visor$tempV.rotateX(xRotation.get());
            this.visor$tempV.rotateZ(yRotation.get());

            // +24 because it should be the offset to the default position, which is at 24
            this.visor$tempV.add(getParentModel().body.x, getParentModel().body.y + 24F, getParentModel().body.z);

            // no yaw, since we  need the vector to be player rotated anyway
            ModelUtils.modelToWorld(player, this.visor$tempV, vrPlayer, 0F, false, false, this.visor$tempV);
            original.call(poseStack, this.visor$tempV.x, -this.visor$tempV.y, -this.visor$tempV.z);
        } else {
            original.call(poseStack, x, y, z);
        }
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isCrouching()Z"), ordinal = 7)
    private float visor$modifyXRot(
        float xRot, @Local(argsOnly = true) AbstractClientPlayer player,
        @Local(ordinal = 2, argsOnly = true) float partialTick, @Share("xRot") LocalFloatRef xRotation)
    {
        if (VRClientPlayers.isTracked(player)) {
            // rotate the cape with the body
            // cancel out crouch
            if (player.isCrouching()) {
                xRot -= 25F;
            }
            // rotate with body
            // max of 0 to keep it down when the body bends backwards
            float min = (player.isFallFlying() ? 1F : player.getSwimAmount(partialTick)) * -Mth.HALF_PI;
            xRot += Mth.RAD_TO_DEG * Math.max(min, xRotation.get());
        }
        return xRot;
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isCrouching()Z"), ordinal = 8)
    private float visor$limitSpeedRot(
        float speedRot, @Local(argsOnly = true) AbstractClientPlayer player,
        @Share("xRot") LocalFloatRef xRotation)
    {
        if (VRClientPlayers.isTracked(player)) {
            // limit the up rotation when walking forward, depending on body rotation
            float rot = xRotation.get() / Mth.HALF_PI;
            if (rot >= 0) {
                return speedRot * (1F - Mth.clamp(rot, 0F, 1F));
            } else {
                return 0F;
            }
        }
        return speedRot;
    }

    @ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 2))
    private float visor$modifyYRotation(
        float yRot, @Local(argsOnly = true) AbstractClientPlayer player,
        @Share("yRot") LocalFloatRef yRotation)
    {
        if (VRClientPlayers.isTracked(player)) {
            // rotate the cape with side body rotation
            yRot += Mth.RAD_TO_DEG * yRotation.get();
        }
        return yRot;
    }
}

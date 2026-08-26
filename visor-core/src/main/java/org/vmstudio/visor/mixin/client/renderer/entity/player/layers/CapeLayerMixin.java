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
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.BackLayerPlacement;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    @Unique
    private final BackLayerPlacement visor$placement = new BackLayerPlacement();

    @Unique
    private final Vector3f visor$offset = new Vector3f();

    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$modifyOffset(
        PoseStack poseStack, float x, float y, float z, Operation<Void> original,
        @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true, ordinal = 2) float partialTick,
        @Share("xRot") LocalFloatRef xRotation, @Share("yRot") LocalFloatRef yRotation)
    {
        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer == null) {
            original.call(poseStack, x, y, z);
            return;
        }

        PlayerModel<AbstractClientPlayer> model = getParentModel();
        visor$placement.aim(model.body, true);
        xRotation.set(visor$placement.pitch());
        yRotation.set(visor$placement.yaw());

        visor$offset.set(0F, 0F, BackLayerPlacement.restingDepth(model.body));
        if (!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            visor$offset.add(0F, -0.85F, 1.1F);
        }

        visor$placement.place(player, vrPlayer, model.body, visor$offset, visor$offset);
        original.call(poseStack, visor$offset.x, -visor$offset.y, -visor$offset.z);
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isCrouching()Z"), ordinal = 7)
    private float visor$modifyXRot(
        float xRot, @Local(argsOnly = true) AbstractClientPlayer player,
        @Local(ordinal = 2, argsOnly = true) float partialTick, @Share("xRot") LocalFloatRef xRotation)
    {
        if (VRClientPlayers.isTracked(player)) {
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
            float leanFraction = xRotation.get() / Mth.HALF_PI;
            if (leanFraction < 0F) {
                return 0F;
            }
            return speedRot * (1F - Mth.clamp(leanFraction, 0F, 1F));
        }
        return speedRot;
    }

    @ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 2))
    private float visor$modifyYRotation(
        float yRot, @Local(argsOnly = true) AbstractClientPlayer player,
        @Share("yRot") LocalFloatRef yRotation)
    {
        if (VRClientPlayers.isTracked(player)) {
            yRot += Mth.RAD_TO_DEG * yRotation.get();
        }
        return yRot;
    }
}

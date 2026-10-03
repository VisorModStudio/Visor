// #!MC-VERSION:: 1.21.10-26.2
package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.BackLayerPlacement;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AvatarRenderState, PlayerModel> {

    @Unique
    private static final float ARMOR_CLEARANCE_Y = -0.85F;
    @Unique
    private static final float ARMOR_CLEARANCE_Z = 1.1F;
    @Unique
    private final BackLayerPlacement visor$placement = new BackLayerPlacement();

    @Unique
    private final Vector3f visor$offset = new Vector3f();

    public CapeLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$noArmorShift(PoseStack poseStack, float x, float y, float z, Operation<Void> original,
                                    @Local(argsOnly = true) AvatarRenderState state) {
        if (visor$vrPlayer(state) == null) {
            original.call(poseStack, x, y, z);
        }
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void visor$capeAnchor(
        SubmitNodeCollector collector, Model model, Object renderState, PoseStack poseStack, RenderType renderType,
        int packedLight, int packedOverlay, int tint, ModelFeatureRenderer.CrumblingOverlay crumbling,
        Operation<Void> original, @Local(argsOnly = true) AvatarRenderState state)
    {
        HumanoidModel<?> capeModel = (HumanoidModel<?>) model;
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        VRClientPlayer vrPlayer = player == null ? null : VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer == null || !capeModel.body.hasChild("cape")) {
            original.call(collector, model, renderState, poseStack, renderType,
                    packedLight, packedOverlay, tint, crumbling);
            return;
        }

        PlayerModel parentModel = getParentModel();
        visor$placement.aim(parentModel.body, true);
        float bodyPitch = visor$placement.pitch();
        // read the slot, not the render state: 1.21.4 renamed its chestItem field to chestEquipment
        boolean armor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();

        visor$offset.set(0F, 0F, BackLayerPlacement.restingDepth(parentModel.body));
        if (armor) {
            visor$offset.add(0F, ARMOR_CLEARANCE_Y, ARMOR_CLEARANCE_Z);
        }
        visor$placement.place(vrPlayer, parentModel.body, visor$offset, visor$offset);
        poseStack.translate(visor$offset.x, -visor$offset.y, -visor$offset.z);

        float flatten = state.isFallFlying ? 1F : state.swimAmount;
        float capePitch = state.capeFlap + Math.max(bodyPitch, -Mth.HALF_PI * flatten) * Mth.RAD_TO_DEG;
        float leanFraction = bodyPitch / Mth.HALF_PI;
        float walkLift = leanFraction < 0F ? 0F : state.capeLean * (1F - Math.min(leanFraction, 1F));
        McRenderUtils.rotate(poseStack, Axis.XP.rotationDegrees(6.0F + walkLift / 2.0F + capePitch));
        McRenderUtils.rotate(poseStack, Axis.ZP.rotationDegrees(state.capeLean2 / 2.0F));
        McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F - state.capeLean2 / 2.0F + Mth.RAD_TO_DEG * visor$placement.yaw()));

        original.call(collector, model, renderState, poseStack, renderType,
                packedLight, packedOverlay, tint, crumbling);
    }

    @Unique
    private static VRClientPlayer visor$vrPlayer(AvatarRenderState state) {
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        return player == null ? null : VRClientPlayers.getPlayer(player.getUUID());
    }
}

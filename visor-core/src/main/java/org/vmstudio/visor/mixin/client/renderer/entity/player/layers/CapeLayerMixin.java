package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.utils.ModelUtils;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;

// 1.21.2: CapeLayer is keyed on PlayerRenderState and PlayerModel is no longer generic.
// 1.21.4: the layer no longer orients the cape itself - the flap/lean values are baked into the
// render state and applied by PlayerCapeModel, so the VR orientation is applied to the pose stack
// here and the vanilla one is suppressed in PlayerCapeModelMixin.
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<PlayerRenderState, PlayerModel> {

    @Unique
    private final Vector3f visor$tempV = new Vector3f();

    @Unique
    private final Matrix3f visor$bodyRot = new Matrix3f();

    public CapeLayerMixin(RenderLayerParent<PlayerRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    // DEBUG CAPE
    /*
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/PlayerSkin;capeTexture()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation visor$whiteCape(PlayerSkin skin, Operation<ResourceLocation> original) {
        ResourceLocation capeTexture = original.call(skin);
        if (capeTexture == null) {
            capeTexture = RenderHelper.WHITE_TEXTURE;
        }
        return capeTexture;
    }
    */

    // ordinal 1 is the HUMANOID check that applies the vanilla with-armor cape offset; returning
    // false there skips it, and the VR offset/rotation is applied instead.
    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;hasLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;)Z", ordinal = 1))
    private boolean visor$modifyTransform(
        boolean hasArmor, @Local(argsOnly = true) PlayerRenderState renderState,
        @Local(argsOnly = true) PoseStack poseStack)
    {
        // entity-derived VR data is resolved during extractRenderState and parked on the state
        var vrPlayer = ((EntityRenderStateExtension) renderState).visor$getVRPlayer();
        if (vrPlayer == null) {
            return hasArmor;
        }

        this.visor$bodyRot.rotationZYX(getParentModel().body.zRot, -getParentModel().body.yRot,
            -getParentModel().body.xRot);

        // attach the cape to the body
        this.visor$bodyRot.transform(VRMathUtils.UP_VECTOR, this.visor$tempV);
        float xRotation = (float) Math.atan2(this.visor$tempV.y, this.visor$tempV.z) - Mth.HALF_PI;

        // make sure it doesn't go below -PI
        xRotation = xRotation < -Mth.PI ? xRotation + Mth.TWO_PI : xRotation;

        this.visor$bodyRot.transform(VRMathUtils.RIGHT_VECTOR, this.visor$tempV);
        float yRotation = (float) -Math.atan2(this.visor$tempV.x, this.visor$tempV.y) + Mth.HALF_PI;

        // transform offset to be body relative
        this.visor$tempV.set(0F, 0F, 2F - 0.5F * (getParentModel().body.xRot / Mth.HALF_PI));
        if (hasArmor) {
            // vanilla cape offset with armor
            this.visor$tempV.add(0F, -0.85F, 1.1F);
        }
        this.visor$tempV.rotateX(xRotation);
        this.visor$tempV.rotateZ(yRotation);

        // +24 because it should be the offset to the default position, which is at 24
        this.visor$tempV.add(getParentModel().body.x, getParentModel().body.y + 24F, getParentModel().body.z);

        // no yaw, since we  need the vector to be player rotated anyway
        ModelUtils.modelToWorld(vrPlayer.getMcPlayer(), this.visor$tempV, vrPlayer, 0F, false, false,
            this.visor$tempV);
        poseStack.translate(this.visor$tempV.x, -this.visor$tempV.y, -this.visor$tempV.z);

        // rotate with body
        // max of 0 to keep it down when the body bends backwards
        float min = (renderState.isFallFlying ? 1F : renderState.swimAmount) * -Mth.HALF_PI;
        float flap = renderState.capeFlap + Mth.RAD_TO_DEG * Math.max(min, xRotation);

        // limit the up rotation when walking forward, depending on body rotation
        float lean = xRotation / Mth.HALF_PI;
        if (lean >= 0) {
            lean = (renderState.isCrouching ? renderState.capeLean - Mth.HALF_PI * 0.5F : renderState.capeLean) *
                (1F - Mth.clamp(lean, 0F, 1F));
        } else {
            lean = 0F;
        }

        // manual rotation, PlayerCapeModel doesn't apply its own for VR players
        poseStack.mulPose(new Quaternionf()
            .rotateX((6.0F + lean / 2.0F + flap) * Mth.DEG_TO_RAD)
            .rotateZ(renderState.capeLean2 / 2.0F * Mth.DEG_TO_RAD)
            .rotateY(-(-renderState.capeLean2 / 2.0F) * Mth.DEG_TO_RAD + yRotation));

        return false;
    }
}

package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.render.player.model.simple.VRPlayerModelSimple;

public class VRPlayerRendererHandsOnly extends PlayerRenderer {
    private static final LayerDefinition VR_LAYER_DEFAULT = LayerDefinition.create(
            CenteredArmsPlayerMesh.create(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VR_LAYER_SLIM = LayerDefinition.create(
            CenteredArmsPlayerMesh.create(CubeDeformation.NONE, true), 64, 64);


    public VRPlayerRendererHandsOnly(EntityRendererProvider.Context context, boolean slim) {
        super(context, slim);
        this.model = new VRPlayerModelSimple<>(
                slim ? VR_LAYER_SLIM.bakeRoot()
                        : VR_LAYER_DEFAULT.bakeRoot(),
                slim
        );
    }

    @Override
    public void render(
            AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight)
    {
        poseStack.pushPose();

        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer != null) {
            var pose = vrPlayer.getPoseData(PlayerPoseType.RENDER);

            float scale = vrPlayer.getModelScale(PlayerPoseType.RENDER);

            if (player.isAutoSpinAttack() && !VRRenderState.getPhase().isVRGui()) {
                float pitchOffset = 0.2F * (player.getViewXRot(partialTick) / 90F);
                poseStack.translate(0, pose.getHmd().getPosition().y() + pitchOffset, 0);
            }

            poseStack.scale(scale, scale, scale);
        }

        super.render(player, entityYaw, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();

        if (vrPlayer != null && VRRenderState.isSpectatedVRView(player)) {
           ClientContext.handRenderer.renderSpectatedHands(
                    this, player, vrPlayer, poseStack, buffer, packedLight, partialTick);
        }
    }


    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        if (VRRenderState.isSpectatedVRView(player)) {
            var model = this.getModel();
            model.head.visible = false;
            model.hat.visible = false;
            model.body.visible = false;
            model.jacket.visible = false;
            model.leftArm.visible = false;
            model.rightArm.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
            model.leftLeg.visible = false;
            model.rightLeg.visible = false;
            model.leftPants.visible = false;
            model.rightPants.visible = false;
        }
    }

    @Override
    protected void setupRotations(
            AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick)
    {
        if (VRRenderState.getPhase().isVRGui()) {
            if (player.isFallFlying() || player.isVisuallySwimming() || player.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
                return;
            }
            super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
            return;
        }

        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer != null) {
            rotationYaw = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw() * Mth.RAD_TO_DEG;
        }

        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
    }



    @Override
    public void renderRightHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        renderVRHand(poseStack, buffer, combinedLight, player, ControllerType.RIGHT);
    }

    @Override
    public void renderLeftHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        renderVRHand(poseStack, buffer, combinedLight, player, ControllerType.LEFT);
    }


    private void renderVRHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            AbstractClientPlayer player, ControllerType side)
    {
        this.setModelProperties(player);

        boolean left = side == ControllerType.LEFT;
        ModelPart arm = left ? this.model.leftArm : this.model.rightArm;
        ModelPart sleeve = left ? this.model.leftSleeve : this.model.rightSleeve;

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        boolean slim = this.getModel().slim;
        arm.setPos(CenteredArmsPlayerMesh.armPivotX(slim, left),
                CenteredArmsPlayerMesh.armPivotY(slim), 0F);
        arm.setRotation(0F, 0F, 0F);
        arm.xScale = 1F;
        arm.yScale = 1F;
        arm.zScale = 1F;
        arm.visible = true;

        sleeve.copyFrom(arm);
        PlayerModelPart sleevePart = left ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;
        sleeve.visible = player.isModelPartShown(sleevePart);

        ResourceLocation skin = this.getTextureLocation(player);
        var consumer = buffer.getBuffer(RenderType.entityTranslucent(skin));
        arm.render(poseStack, consumer, combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        sleeve.render(poseStack, consumer, combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
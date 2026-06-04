package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.client.Minecraft;
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
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.render.player.model.simple.VRPlayerModelSimple;
import org.vmstudio.visor.core.client.utils.ScaleHelper;

public class VRPlayerRendererHandsOnly extends PlayerRenderer {
    private static LayerDefinition VR_LAYER_DEFAULT;
    private static LayerDefinition VR_LAYER_SLIM;
    static {
        createLayers();
    }

    public static void createLayers() {
        VR_LAYER_DEFAULT = LayerDefinition.create(
                CenteredArmsPlayerMesh.create(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_SLIM = LayerDefinition.create(
                CenteredArmsPlayerMesh.create(CubeDeformation.NONE, true), 64, 64);
    }


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

            float scale = vrPlayer.getFullHeightScale();
            if ((VisorState.get().isActive()
                    && player == Minecraft.getInstance().player))
            {
                scale *= pose.getWorldScale() / ScaleHelper.getEntityEyeHeightScale(player, partialTick);
            }

            if (player.isAutoSpinAttack() && !VRRenderState.getPhase().isVRGui()) {
                float offset = player.getViewXRot(partialTick) / 90F * 0.2F;
                poseStack.translate(0, pose.getHmd().getPosition().y() + offset, 0);
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

        // vanilla below here
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
    }



    @Override
    public void renderRightHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.renderHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, player, this.model.rightArm,
                this.model.rightSleeve);
    }

    @Override
    public void renderLeftHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.renderHand(ControllerType.LEFT, poseStack, buffer, combinedLight, player, this.model.leftArm,
                this.model.leftSleeve);
    }


    private void renderHand(
            ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear)
    {
        this.setModelProperties(player);

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        boolean slim = this.getModel().slim;
        boolean left = side == ControllerType.LEFT;
        rendererArm.setPos(CenteredArmsPlayerMesh.armPivotX(slim, left),
                CenteredArmsPlayerMesh.armPivotY(slim), 0F);
        rendererArm.setRotation(0F, 0F, 0F);
        rendererArm.xScale = rendererArm.yScale = rendererArm.zScale = 1F;
        rendererArm.visible = true;

        rendererArmwear.copyFrom(rendererArm);
        rendererArmwear.visible = true;

        ResourceLocation playerSkin = this.getTextureLocation(player);

        // render hand
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0f);

        // render armor
        rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0f);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
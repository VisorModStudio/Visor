package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.render.player.model.full.VRPlayerModelFull;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.core.client.utils.ScaleHelper;


public class VRPlayerRendererFull extends PlayerRenderer {
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


    public VRPlayerRendererFull(EntityRendererProvider.Context context, boolean slim) {
        super(context, slim);
        this.model = new VRPlayerModelFull<>(
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
            if ((VisorState.get().isActive() && player == Minecraft.getInstance().player)) {
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
    public Vec3 getRenderOffset(AbstractClientPlayer player, float partialTick) {
        if (VRRenderState.isSelfModelPlayer(player)) {
            return player.isVisuallySwimming() ?
                    new Vec3(0.0F, -0.125F * ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale(), 0.0F) : Vec3.ZERO;
        } else {
            return player.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        }
    }

    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        this.getModel().crouching &= !player.isVisuallySwimming();
        if (VRRenderState.isSelfModelRender(player)) {
            this.model.head.visible = false;
            this.model.hat.visible = false;

            VRBodyType.ModelSelfVisibility visibility =
                    ClientContext.localPlayer.getBodyType().getSelfModelVisibility();
            if (visibility == VRBodyType.ModelSelfVisibility.WITHOUT_HANDS
                    && this.getModel() instanceof VRPlayerModelFull<?> vrModel) {
                vrModel.hideLeftArm();
                vrModel.hideRightArm();
            }
        } else if (VRRenderState.isSpectatedVRView(player)) {
            this.model.head.visible = false;
            this.model.hat.visible = false;
            if (this.getModel() instanceof VRPlayerModelFull<?> vrModel) {
                vrModel.hideLeftArm();
                vrModel.hideRightArm();
            }
        }
    }


    @Override
    public void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        renderVRHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, player);
    }

    @Override
    public void renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        renderVRHand(ControllerType.LEFT, poseStack, buffer, combinedLight, player);
    }

    private void renderVRHand(
            ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            AbstractClientPlayer player) {
        this.setModelProperties(player);

        boolean left = side == ControllerType.LEFT;
        ModelPart arm = left ? this.model.leftArm : this.model.rightArm;
        ModelPart sleeve = left ? this.model.leftSleeve : this.model.rightSleeve;

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        boolean slim = this.getModel().slim;
        arm.setPos(CenteredArmsPlayerMesh.armPivotX(slim, left),
                CenteredArmsPlayerMesh.armPivotY(slim), 0F);
        arm.setRotation(0F, 0F, 0F);
        arm.xScale = arm.yScale = arm.zScale = 1F;
        arm.visible = true;
        sleeve.copyFrom(arm);
        sleeve.visible = true;

        ResourceLocation skin = this.getTextureLocation(player);

        arm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(skin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0f);
        sleeve.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(skin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0f);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
}
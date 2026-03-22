package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.VRPlayerModel;
import org.vmstudio.visor.core.client.render.player.modeltest.VRPlayerModelTest;
import org.vmstudio.visor.core.client.render.player.modeltest.armor.VRArmorLayerTest;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ScaleHelper;

public class VRPlayerRendererTest extends PlayerRenderer {    // Vanilla model

    // split arms model


    static {
        createLayers();
    }

    public static void createLayers() {

    }


    public VRPlayerRendererTest(EntityRendererProvider.Context context, boolean slim, VRClientSettings.PlayerModelType type) {
        super(context, slim);

        VRArmorLayerTest.createLayers();
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
                // remove entity scale, since the entity is already scaled by that before
                scale *= pose.getWorldScale() / ScaleHelper.getEntityEyeHeightScale(player, partialTick);
            }

            if (player.isAutoSpinAttack()) {
                // offset player to head
                float offset = player.getViewXRot(partialTick) / 90F * 0.2F;
                poseStack.translate(0, pose.getHmd().getPosition().y() + offset, 0);
            }

            poseStack.scale(scale, scale, scale);
        }

        super.render(player, entityYaw, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        // no crouch hip movement when roomscale crawling
        this.getModel().crouching &= !player.isVisuallySwimming();

        if (VRRenderState.isSelfModelRender(player)) {
            // hide the head or you won't see anything
            this.model.body.visible = false;
            this.model.jacket.visible = false;
            this.model.leftLeg.visible = false;
            this.model.rightLeg.visible = false;
            this.model.leftPants.visible = false;
            this.model.rightPants.visible = false;
            this.model.head.visible = false;
            this.model.hat.visible = false;
        }
    }


    @Override
    protected void setupRotations(
            AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick)
    {
        if (!VRRenderState.getPhase().isVRGui()){
            var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
            if(vrPlayer == null) {
                return;
            }
            rotationYaw = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw();
        }

        // vanilla below here
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw * Mth.RAD_TO_DEG, partialTick);
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
        PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
        this.setModelProperties(player);

        // blending, since we render the arm translucent
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // in case some mod messes with it
        rendererArm.setPos(side == ControllerType.LEFT ? 5F : -5F, 2F, 0F);
        rendererArm.setRotation(0F, 0F, 0F);
        rendererArm.xScale = rendererArm.yScale = rendererArm.zScale = 1F;

        // make sure they have the same state
        rendererArmwear.copyFrom(rendererArm);

        float alpha = player.getAttackStrengthScale(0.0F) * 0.75F + 0.25F;
        ResourceLocation playerSkin = this.getTextureLocation(player);

        // render hand
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);

        // render armor
        rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

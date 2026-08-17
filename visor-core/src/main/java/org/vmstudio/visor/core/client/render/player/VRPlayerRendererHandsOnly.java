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
import net.minecraft.world.entity.player.PlayerModelPart;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.render.player.model.simple.VRPlayerModelSimple;
import org.vmstudio.visor.core.client.utils.ScaleHelper;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;
import org.vmstudio.visor.extensions.client.entity.PlayerRendererExtension;

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
        this.model = new VRPlayerModelSimple(
                slim ? VR_LAYER_SLIM.bakeRoot()
                        : VR_LAYER_DEFAULT.bakeRoot(),
                slim
        );
    }


    @Override
    public void extractRenderState(AbstractClientPlayer player,
                                   PlayerRenderState renderState,
                                   float partialTick) {
        super.extractRenderState(player, renderState, partialTick);

        EntityRenderStateExtension ext = (EntityRenderStateExtension) renderState;
        ext.visor$setVRPlayer(VRClientPlayers.getPlayer(player.getUUID()));
        ext.visor$setSelfModelRender(VRRenderState.isSelfModelRender(player));
        ext.visor$setSelfModelPlayer(VRRenderState.isSelfModelPlayer(player));
        ext.visor$setSelfModelHandsRender(VRRenderState.isSelfModelHandsRender(player));
    }

    @Override
    public void render(PlayerRenderState renderState, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight)
    {

        poseStack.pushPose();

        var vrPlayer = ((EntityRenderStateExtension) renderState).visor$getVRPlayer();

        if (vrPlayer != null) {
            var pose = vrPlayer.getPoseData(PlayerPoseType.RENDER);
            AbstractClientPlayer player = (AbstractClientPlayer) vrPlayer.getMcPlayer();
            float partialTick = ClientContext.visor != null
                    ? ClientContext.visor.getPartialTicks()
                    : 1.0F;

            float scale = vrPlayer.getFullHeightScale();
            if ((VisorState.get().isActive()
                    && player == Minecraft.getInstance().player))
            {
                scale *= pose.getWorldScale() / ScaleHelper.getEntityEyeHeightScale(player, partialTick);
            }

            if (renderState.isAutoSpinAttack && !VRRenderState.getPhase().isVRGui()) {
                float offset = renderState.xRot / 90F * 0.2F;
                poseStack.translate(0, pose.getHmd().getPosition().y() + offset, 0);
            }

            poseStack.scale(scale, scale, scale);
        }

        // Not super.render(...): on Forge/NeoForge that binds to a synthetic bridge in
        // PlayerRenderer and recurses back into this method. See PlayerRenderMixins.
        ((PlayerRendererExtension) this).visor$renderVanilla(renderState, poseStack, buffer, packedLight);

        poseStack.popPose();

        if (vrPlayer != null && VRRenderState.isSpectatedVRView(vrPlayer.getMcPlayer())) {
           ClientContext.handRenderer.renderSpectatedHands(
                    this, renderState, (AbstractClientPlayer) vrPlayer.getMcPlayer(), vrPlayer, poseStack,
                    buffer, packedLight,
                    ClientContext.visor != null ? ClientContext.visor.getPartialTicks() : 1.0F);
        }
    }


    @Override
    protected void setupRotations(
            PlayerRenderState renderState, PoseStack poseStack, float bodyRot, float scale)
    {
        if (VRRenderState.getPhase().isVRGui()) {
            if (renderState.isFallFlying || renderState.isVisuallySwimming || renderState.isAutoSpinAttack) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
                return;
            }
            super.setupRotations(renderState, poseStack, bodyRot, scale);
            return;
        }

        var vrPlayer = ((EntityRenderStateExtension) renderState).visor$getVRPlayer();
        if (vrPlayer != null) {
            bodyRot = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw() * Mth.RAD_TO_DEG;
        }

        // vanilla below here
        super.setupRotations(renderState, poseStack, bodyRot, scale);
    }




    @Override
    public void renderRightHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin,
            boolean isSleeveVisible)
    {
        this.renderHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, skin, isSleeveVisible,
                this.model.rightArm, this.model.rightSleeve);
    }

    @Override
    public void renderLeftHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin,
            boolean isSleeveVisible)
    {
        this.renderHand(ControllerType.LEFT, poseStack, buffer, combinedLight, skin, isSleeveVisible,
                this.model.leftArm, this.model.leftSleeve);
    }


    private void renderHand(
            ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            ResourceLocation playerSkin, boolean isSleeveVisible,
            ModelPart rendererArm, ModelPart rendererArmwear)
    {
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


        rendererArmwear.resetPose();
        rendererArmwear.visible = isSleeveVisible;

        // render hand (and, as its child, the sleeve)
        rendererArm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(playerSkin)), combinedLight,
                OverlayTexture.NO_OVERLAY);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
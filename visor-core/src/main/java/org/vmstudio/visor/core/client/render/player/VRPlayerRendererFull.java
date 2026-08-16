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
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.core.client.utils.ScaleHelper;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;
import org.vmstudio.visor.extensions.client.entity.PlayerRendererExtension;


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
        this.model = new VRPlayerModelFull(
                slim ? VR_LAYER_SLIM.bakeRoot()
                        : VR_LAYER_DEFAULT.bakeRoot(),
                slim
        );
    }

    // 1.21.2: everything entity-derived has to be resolved here, because render() and the
    // model only ever see the PlayerRenderState snapshot.
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
                // remove entity scale, since the entity is already scaled by that before
                scale *= pose.getWorldScale() / ScaleHelper.getEntityEyeHeightScale(player, partialTick);
            }

            if (renderState.isAutoSpinAttack && !VRRenderState.getPhase().isVRGui()) {
                // offset player to head
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
    public Vec3 getRenderOffset(PlayerRenderState renderState) {
        if (((EntityRenderStateExtension) renderState).visor$isSelfModelPlayer()) {
            return renderState.isVisuallySwimming ?
                    new Vec3(0.0F, -0.125F * ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getWorldScale(), 0.0F) : Vec3.ZERO;
        } else {
            return renderState.isVisuallySwimming ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        }
    }

    // 1.21.2 removed setModelProperties; the part visibility it applied now lives in
    // VRPlayerModelFull#setupAnim, which is the only hook that runs after the model resets.


    // 1.21.2: the hand renderers take a resolved skin and sleeve flag, not the player
    @Override
    public void renderRightHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin,
            boolean isSleeveVisible)
    {
        renderVRHand(ControllerType.RIGHT, poseStack, buffer, combinedLight, skin);
    }

    @Override
    public void renderLeftHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin,
            boolean isSleeveVisible)
    {
        renderVRHand(ControllerType.LEFT, poseStack, buffer, combinedLight, skin);
    }

    private void renderVRHand(
            ControllerType side, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            ResourceLocation skin)
    {
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
        // 1.21.2: the sleeve is a child of the arm at PartPose.ZERO, so it is drawn by
        // arm.render and must not be posed or rendered separately.
        sleeve.resetPose();
        sleeve.visible = true;

        arm.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(skin)), combinedLight,
                OverlayTexture.NO_OVERLAY);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // 1.21.2: setupRotations lost ageInTicks and partialTick - both are baked into the state
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

}
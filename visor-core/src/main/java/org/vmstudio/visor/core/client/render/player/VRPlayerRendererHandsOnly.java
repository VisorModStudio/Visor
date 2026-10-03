package org.vmstudio.visor.core.client.render.player;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.ControllerType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
//? if >=26.2 {
import net.minecraft.client.model.Model;
import net.minecraft.util.Unit;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
//? if >=1.21.9 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//?} else {
/*import net.minecraft.client.renderer.entity.player.PlayerRenderer;
*///?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.CenteredArmsPlayerMesh;
import org.vmstudio.visor.core.client.render.player.model.simple.VRPlayerModelSimple;
//? if >=1.21.9 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?} else {
/*import net.minecraft.world.entity.player.PlayerModelPart;
*///?}

//? if >=1.21.9 {
public class VRPlayerRendererHandsOnly extends AvatarRenderer<AbstractClientPlayer> {
//?} else {
/*public class VRPlayerRendererHandsOnly extends PlayerRenderer {
*///?}
    private static final LayerDefinition VR_LAYER_DEFAULT = LayerDefinition.create(
            CenteredArmsPlayerMesh.create(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VR_LAYER_SLIM = LayerDefinition.create(
            CenteredArmsPlayerMesh.create(CubeDeformation.NONE, true), 64, 64);


    public VRPlayerRendererHandsOnly(EntityRendererProvider.Context context, boolean slim) {
        super(context, slim);
        this.model = new VRPlayerModelSimple(
                slim ? VR_LAYER_SLIM.bakeRoot()
                        : VR_LAYER_DEFAULT.bakeRoot(),
                slim
        );
        //? if >=1.21.9 {
        ArmorModelSet<PlayerModel> armorModels = ArmorModelSet.bake(
                slim ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR,
                context.getModelSet(), part -> new VRPlayerModelSimple(part, slim));
        this.layers.replaceAll(layer -> layer instanceof HumanoidArmorLayer<?, ?, ?>
                ? new HumanoidArmorLayer<>(this, armorModels, context.getEquipmentRenderer())
                : layer);
        //?}
    }

    //? if >=1.21.9 {
    @Override
    public AvatarRenderState createRenderState() {
        return new VRPlayerRenderState();
    }

    @Override
    public void extractRenderState(AbstractClientPlayer player, AvatarRenderState state, float partialTick) {
        super.extractRenderState(player, state, partialTick);
        VRPlayerRenderState.extract(state, player, partialTick);
    }

    @Override
    public void submit(AvatarRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (!(state instanceof VRPlayerRenderState vrState) || vrState.player == null) {
            super.submit(state, poseStack, collector, camera);
            return;
        }
        renderVR(vrState.player, vrState.partialTick, this.getRenderOffset(state),
                //? if >=26.2 {
                poseStack, collector, state.lightCoords,
                //?} else {
                /*poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), state.lightCoords,
                *///?}
                () -> super.submit(state, poseStack, collector, camera));
    }
    //?} elif >=1.21.2 {
    /*@Override
    public PlayerRenderState createRenderState() {
        return new VRPlayerRenderState();
    }

    @Override
    public void extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float partialTick) {
        super.extractRenderState(player, state, partialTick);
        VRPlayerRenderState.extract(state, player, partialTick);
    }

    @Override
    public void render(PlayerRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!(state instanceof VRPlayerRenderState vrState) || vrState.player == null) {
            super.render(state, poseStack, buffer, packedLight);
            return;
        }
        renderVR(vrState.player, vrState.partialTick, this.getRenderOffset(state),
                poseStack, buffer, packedLight,
                () -> super.render(state, poseStack, buffer, packedLight));
    }
    *///?} else {
    /*@Override
    public void render(
            AbstractClientPlayer player, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight)
    {
        renderVR(player, partialTick, this.getRenderOffset(player, partialTick),
                poseStack, buffer, packedLight,
                () -> super.render(player, entityYaw, partialTick, poseStack, buffer, packedLight));
    }

    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        if (this.model instanceof VRPlayerModelSimple vrModel) {
            vrModel.applyVisibility(player);
        }
    }
    *///?}

    private void renderVR(AbstractClientPlayer player, float partialTick, Vec3 renderOffset,
                          //? if >=26.2 {
                          PoseStack poseStack, SubmitNodeCollector buffer, int packedLight,
                          //?} else {
                          /*PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                          *///?}
                          Runnable vanillaRender) {
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

        vanillaRender.run();

        poseStack.popPose();

        if (vrPlayer != null && VRRenderState.isSpectatedVRView(player)) {
           ClientContext.handRenderer.renderSpectatedHands(
                    renderOffset, player, vrPlayer, poseStack, buffer, packedLight, partialTick);
        }
    }


    //? if >=1.21.9 {
    @Override
    protected void setupRotations(AvatarRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        if (VRRenderState.getPhase().isVRGui()) {
            if (state.isFallFlying || state.isVisuallySwimming || state.isAutoSpinAttack) {
                McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F - bodyRot));
                return;
            }
        } else {
            bodyRot = vrBodyYaw(VRPlayerRenderState.playerOf(state), bodyRot);
        }
        super.setupRotations(state, poseStack, bodyRot, scale);
    }
    //?} elif >=1.21.2 {
    /*@Override
    protected void setupRotations(PlayerRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        if (VRRenderState.getPhase().isVRGui()) {
            if (state.isFallFlying || state.isVisuallySwimming || state.isAutoSpinAttack) {
                McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F - bodyRot));
                return;
            }
        } else {
            bodyRot = vrBodyYaw(VRPlayerRenderState.playerOf(state), bodyRot);
        }
        super.setupRotations(state, poseStack, bodyRot, scale);
    }
    *///?} elif >=1.20.5 {
    /*@Override
    protected void setupRotations(
            AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale)
    {
        if (VRRenderState.getPhase().isVRGui()) {
            if (player.isFallFlying() || player.isVisuallySwimming() || player.isAutoSpinAttack()) {
                McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F - rotationYaw));
                return;
            }
        } else {
            rotationYaw = vrBodyYaw(player, rotationYaw);
        }
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick, scale);
    }
    *///?} else {
    /*@Override
    protected void setupRotations(
            AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick)
    {
        if (VRRenderState.getPhase().isVRGui()) {
            if (player.isFallFlying() || player.isVisuallySwimming() || player.isAutoSpinAttack()) {
                McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F - rotationYaw));
                return;
            }
        } else {
            rotationYaw = vrBodyYaw(player, rotationYaw);
        }
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
    }
    *///?}

    private static float vrBodyYaw(AbstractClientPlayer player, float vanillaYaw) {
        var vrPlayer = player == null ? null : VRClientPlayers.getPlayer(player.getUUID());
        if (vrPlayer == null) {
            return vanillaYaw;
        }
        return vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw() * Mth.RAD_TO_DEG;
    }


    //? if >=1.21.9 {
    @Override
    public void renderRightHand(PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, Identifier skin, boolean sleeveVisible) {
        //? if >=26.2 {
        renderVRHand(poseStack, collector, combinedLight, skin, sleeveVisible, ControllerType.RIGHT);
        //?} else {
        /*renderVRHand(poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), combinedLight, skin, sleeveVisible, ControllerType.RIGHT);
        *///?}
    }

    @Override
    public void renderLeftHand(PoseStack poseStack, SubmitNodeCollector collector, int combinedLight, Identifier skin, boolean sleeveVisible) {
        //? if >=26.2 {
        renderVRHand(poseStack, collector, combinedLight, skin, sleeveVisible, ControllerType.LEFT);
        //?} else {
        /*renderVRHand(poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), combinedLight, skin, sleeveVisible, ControllerType.LEFT);
        *///?}
    }
    //?} elif >=1.21.2 {
    /*@Override
    public void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin, boolean sleeveVisible) {
        renderVRHand(poseStack, buffer, combinedLight, skin, sleeveVisible, ControllerType.RIGHT);
    }

    @Override
    public void renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation skin, boolean sleeveVisible) {
        renderVRHand(poseStack, buffer, combinedLight, skin, sleeveVisible, ControllerType.LEFT);
    }
    *///?} else {
    /*@Override
    public void renderRightHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.setModelProperties(player);
        renderVRHand(poseStack, buffer, combinedLight, this.getTextureLocation(player),
                player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), ControllerType.RIGHT);
    }

    @Override
    public void renderLeftHand(
            PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player)
    {
        this.setModelProperties(player);
        renderVRHand(poseStack, buffer, combinedLight, this.getTextureLocation(player),
                player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), ControllerType.LEFT);
    }
    *///?}


    private void renderVRHand(
            //? if >=26.2 {
            PoseStack poseStack, SubmitNodeCollector buffer, int combinedLight,
            //?} else {
            /*PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
            *///?}
            Identifier skin, boolean sleeveVisible, ControllerType side)
    {
        boolean left = side == ControllerType.LEFT;
        ModelPart arm = left ? this.model.leftArm : this.model.rightArm;
        ModelPart sleeve = left ? this.model.leftSleeve : this.model.rightSleeve;

        McGlState.enableBlend();
        McGlState.enableCull();
        McGlState.blendFuncSeparate(
                McGlState.Blend.SRC_ALPHA,
                McGlState.Blend.ONE_MINUS_SRC_ALPHA,
                McGlState.Blend.ONE,
                McGlState.Blend.ONE_MINUS_SRC_ALPHA
        );

        //? if >=26.2 {
        RenderType renderType = McRenderUtils.entityTranslucent(skin);
        buffer.submitModel(new Model<Unit>(arm, texture -> renderType) {
            @Override
            public void setupAnim(Unit state) {
                poseArm(arm, left);
                sleeve.resetPose();
                sleeve.visible = sleeveVisible;
            }
        //? if >=26.3 {
        }, Unit.INSTANCE, poseStack, renderType, combinedLight, OverlayTexture.NO_OVERLAY, 0);
        //?} else {
        /*}, Unit.INSTANCE, poseStack, renderType, combinedLight, OverlayTexture.NO_OVERLAY, 0, null);
        *///?}
        //?} elif >=1.21.2 {
        /*poseArm(arm, left);
        var consumer = buffer.getBuffer(McRenderUtils.entityTranslucent(skin));
        // the sleeve is a child of the arm since 1.21.2
        sleeve.resetPose();
        sleeve.visible = sleeveVisible;
        McRenderUtils.renderModelPart(arm, poseStack, consumer, combinedLight,
                OverlayTexture.NO_OVERLAY);
        *///?} else {
        /*poseArm(arm, left);
        var consumer = buffer.getBuffer(McRenderUtils.entityTranslucent(skin));
        sleeve.copyFrom(arm);
        sleeve.visible = sleeveVisible;
        McRenderUtils.renderModelPart(arm, poseStack, consumer, combinedLight,
                OverlayTexture.NO_OVERLAY);
        McRenderUtils.renderModelPart(sleeve, poseStack, consumer, combinedLight,
                OverlayTexture.NO_OVERLAY);
        *///?}

        McGlState.disableBlend();
        McGlState.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void poseArm(ModelPart arm, boolean left) {
        boolean slim = this.getModel().slim;
        arm.setPos(CenteredArmsPlayerMesh.armPivotX(slim, left),
                CenteredArmsPlayerMesh.armPivotY(slim), 0F);
        arm.setRotation(0F, 0F, 0F);
        arm.xScale = 1F;
        arm.yScale = 1F;
        arm.zScale = 1F;
        arm.visible = true;
    }
}

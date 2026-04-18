package org.vmstudio.visor.core.client.render.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.full.VRPlayerModelFull;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.core.client.render.player.model.full.armor.VRArmorLayerFull;
import org.vmstudio.visor.core.client.render.player.model.full.armor.VRArmorModelFull;
import org.vmstudio.visor.core.client.utils.ScaleHelper;


public class VRPlayerRendererFull extends PlayerRenderer {    // Vanilla model
    private static LayerDefinition VR_LAYER_DEFAULT;
    private static LayerDefinition VR_LAYER_SLIM;

    static {
        createLayers();
    }

    public static void createLayers() {
        // split arms model
        VR_LAYER_DEFAULT = LayerDefinition.create(
                VRPlayerModelFull.createMesh(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_SLIM = LayerDefinition.create(
                VRPlayerModelFull.createMesh(CubeDeformation.NONE, true), 64, 64);

    }


    public VRPlayerRendererFull(EntityRendererProvider.Context context, boolean slim) {
        super(context, slim);
        this.model = new VRPlayerModelFull<>(
                slim ? VR_LAYER_SLIM.bakeRoot()
                        : VR_LAYER_DEFAULT.bakeRoot(),
                slim
        );


        VRArmorLayerFull.createLayers();

        // remove vanilla armor layer
        this.layers.stream()
                .filter(layer -> layer.getClass() == HumanoidArmorLayer.class)
                .findFirst()
                .ifPresent(this.layers::remove);
        //add custom armor layer
        this.addLayer(new VRArmorLayerFull<>(this,
                new VRArmorModelFull<>(VRArmorLayerFull.VR_ARMOR_DEF_ARMS_INNER.bakeRoot()),
                new VRArmorModelFull<>(VRArmorLayerFull.VR_ARMOR_DEF_ARMS_OUTER.bakeRoot()),
                context.getModelManager()));
    }

    public boolean hasLayerType(RenderLayer<?, ?> renderLayer) {
        return this.layers.stream().anyMatch(layer -> {
            if (renderLayer.getClass() == HumanoidArmorLayer.class) {
                return layer.getClass() == renderLayer.getClass() || layer.getClass() == VRArmorLayerFull.class;
            }
            return layer.getClass() == renderLayer.getClass();
        });
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
    public Vec3 getRenderOffset(AbstractClientPlayer player, float partialTick) {
        // idk why we do this anymore
        // this changes the offset to only apply when swimming, instead of crouching
        if (VRRenderState.isSelfModelPlayer(player)) {
            return player.isVisuallySwimming() ?
                    new Vec3(0.0F, -0.125F * VRClientPlayers.getLocalPlayer().getPoseData(PlayerPoseType.RENDER).getWorldScale(), 0.0F) : Vec3.ZERO;
        } else {
            return player.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        }
    }

    @Override
    public void setModelProperties(AbstractClientPlayer player) {
        super.setModelProperties(player);

        // no crouch hip movement when roomscale crawling
        this.getModel().crouching &= !player.isVisuallySwimming();

        if (VRRenderState.isSelfModelRender(player)) {
            // hide the head or you won't see anything
            this.model.head.visible = false;
            this.model.hat.visible = false;
        }
    }

    private void hideHand(HumanoidArm arm) {
        if (this.getModel() instanceof VRPlayerModelFull<?> vrModel) {
            if (arm == HumanoidArm.LEFT) {
                vrModel.hideLeftArm();
            } else {
                vrModel.hideRightArm();
            }
        } else {
            if (arm == HumanoidArm.LEFT) {
                getModel().leftArm.visible = false;
                getModel().leftSleeve.visible = false;
            } else {
                getModel().rightArm.visible = false;
                getModel().rightSleeve.visible = false;
            }
        }
    }

    @Override
    protected void setupRotations(
            AbstractClientPlayer player, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick)
    {
        if (!VRRenderState.getPhase().isVRGui()){
            var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
            if (vrPlayer != null) {
                rotationYaw = vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw() * Mth.RAD_TO_DEG;
            }
        }

        // vanilla below here
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTick);
    }
}

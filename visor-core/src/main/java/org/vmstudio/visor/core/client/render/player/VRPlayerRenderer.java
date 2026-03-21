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
import org.vmstudio.visor.core.client.render.player.model.VRPlayerModel;
import org.vmstudio.visor.core.client.render.player.model.VRPlayerModelWithArms;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.vmstudio.visor.core.client.render.player.model.VRPlayerModelWithArmsLegs;
import org.vmstudio.visor.core.client.render.player.model.armor.VRArmorLayer;
import org.vmstudio.visor.core.client.render.player.model.armor.VRArmorModelWithArms;
import org.vmstudio.visor.core.client.render.player.model.armor.VRArmorModelWithArmsLegs;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ScaleHelper;


public class VRPlayerRenderer extends PlayerRenderer {    // Vanilla model
    private static final LayerDefinition VR_LAYER_DEF = LayerDefinition.create(
            VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
    private static final LayerDefinition VR_LAYER_DEF_SLIM = LayerDefinition.create(
            VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);

    // split arms model
    private static LayerDefinition VR_LAYER_DEF_ARMS;
    private static LayerDefinition VR_LAYER_DEF_ARMS_SLIM;

    // split arms/legs model
    private static LayerDefinition VR_LAYER_DEF_ARMS_LEGS;
    private static LayerDefinition VR_LAYER_DEF_ARMS_LEGS_SLIM;

    static {
        // need to make these not final, because they change depending on settings
        createLayers();
    }

    public static void createLayers() {
        // split arms model
        VR_LAYER_DEF_ARMS = LayerDefinition.create(
                VRPlayerModelWithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_DEF_ARMS_SLIM = LayerDefinition.create(
                VRPlayerModelWithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

        // split arms/legs
        VR_LAYER_DEF_ARMS_LEGS = LayerDefinition.create(
                VRPlayerModelWithArmsLegs.createMesh(CubeDeformation.NONE, false), 64, 64);
        VR_LAYER_DEF_ARMS_LEGS_SLIM = LayerDefinition.create(
                VRPlayerModelWithArmsLegs.createMesh(CubeDeformation.NONE, true), 64, 64);
    }


    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, VRClientSettings.PlayerModelType type) {
        super(context, slim);
        this.model = switch (type) {
            case VANILLA -> new VRPlayerModel<>(slim ? VR_LAYER_DEF_SLIM.bakeRoot() : VR_LAYER_DEF.bakeRoot(), slim);
            case SPLIT_ARMS ->
                    new VRPlayerModelWithArms<>(slim ? VR_LAYER_DEF_ARMS_SLIM.bakeRoot() : VR_LAYER_DEF_ARMS.bakeRoot(),
                            slim);
            case SPLIT_ARMS_LEGS -> new VRPlayerModelWithArmsLegs<>(
                    slim ? VR_LAYER_DEF_ARMS_LEGS_SLIM.bakeRoot() : VR_LAYER_DEF_ARMS_LEGS.bakeRoot(), slim);
        };


        VRArmorLayer.createLayers();
        if (type != VRClientSettings.PlayerModelType.VANILLA) {
            // remove vanilla armor layer
            this.layers.stream()
                    .filter(layer -> layer.getClass() == HumanoidArmorLayer.class)
                    .findFirst()
                    .ifPresent(this.layers::remove);
            // add split armor layer
            if (type == VRClientSettings.PlayerModelType.SPLIT_ARMS) {
                this.addLayer(new VRArmorLayer<>(this,
                        new VRArmorModelWithArms<>(VRArmorLayer.VR_ARMOR_DEF_ARMS_INNER.bakeRoot()),
                        new VRArmorModelWithArms<>(VRArmorLayer.VR_ARMOR_DEF_ARMS_OUTER.bakeRoot()),
                        context.getModelManager()));
            } else {
                this.addLayer(new VRArmorLayer<>(this,
                        new VRArmorModelWithArmsLegs<>(VRArmorLayer.VR_ARMOR_DEF_ARMS_LEGS_INNER.bakeRoot()),
                        new VRArmorModelWithArmsLegs<>(VRArmorLayer.VR_ARMOR_DEF_ARMS_LEGS_OUTER.bakeRoot()),
                        context.getModelManager()));
            }
        }
    }

    /**
     * @param renderLayer RenderLayer to check
     * @return if a layer of the given class is already registered
     */
    public boolean hasLayerType(RenderLayer<?, ?> renderLayer) {
        return this.layers.stream().anyMatch(layer -> {
            if (renderLayer.getClass() == HumanoidArmorLayer.class) {
                return layer.getClass() == renderLayer.getClass() || layer.getClass() == VRArmorLayer.class;
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
            var pose = vrPlayer.getPoseData(PlayerPoseType.TICK);

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
                    new Vec3(0.0F, -0.125F * VRClientPlayers.getLocalPlayer().getPoseData(PlayerPoseType.TICK).getWorldScale(), 0.0F) : Vec3.ZERO;
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
            this.model.body.visible = false;
            this.model.jacket.visible = false;
            this.model.leftLeg.visible = false;
            this.model.rightLeg.visible = false;
            this.model.leftPants.visible = false;
            this.model.rightPants.visible = false;
            this.model.head.visible = false;
            this.model.hat.visible = false;

            // hide model arms when not using them
            if (VRClientSettings.getModelArmsMode() !=
                    VRClientSettings.ModelArmsMode.COMPLETE)
            {
                // keep the shoulders when in shoulder mode
                hideHand(HumanoidArm.LEFT, VRClientSettings.getModelArmsMode() ==
                        VRClientSettings.ModelArmsMode.OFF);
                hideHand(HumanoidArm.RIGHT, VRClientSettings.getModelArmsMode() ==
                        VRClientSettings.ModelArmsMode.OFF);
            } else {
                /*boolean leftHanded = VRClientSettings.isLeftHanded();
                if (ClientDataHolderVR.getInstance().menuHandOff) {
                    hideHand(leftHanded ? HumanoidArm.RIGHT : HumanoidArm.LEFT, false);
                }
                if (ClientDataHolderVR.getInstance().menuHandMain) {
                    hideHand(leftHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT, false);
                }*/
            }
        }
    }

    private void hideHand(HumanoidArm arm, boolean completeArm) {
        if (this.getModel() instanceof VRPlayerModel<?> vrModel) {
            if (arm == HumanoidArm.LEFT) {
                vrModel.hideLeftArm(completeArm);
            } else {
                vrModel.hideRightArm(completeArm);
            }
        } else {
            // this is just for the case someone replaces the model
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
            if(vrPlayer == null) {
                return;
            }
            rotationYaw = vrPlayer.getPoseData(PlayerPoseType.TICK).getBodyYaw();
        }

        // vanilla below here
        super.setupRotations(player, poseStack, ageInTicks, rotationYaw * Mth.RAD_TO_DEG, partialTick);
    }
}

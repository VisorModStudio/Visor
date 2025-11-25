package me.phoenixra.visor.core.client.render.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class VRPlayerModel<T extends LivingEntity> extends PlayerModel<T> {
    private final ModelPart vrHMD;
    private boolean laying;

    public VRPlayerModel(ModelPart modelPart, boolean isSlim) {
        super(modelPart, isSlim);

        this.vrHMD = modelPart.getChild("vrHMD");
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation,
                                            boolean slim) {
        MeshDefinition meshdefinition = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "vrHMD",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6.0F, -7.5F, 7.0F, 4.0F, 5.0F, cubeDeformation),
                PartPose.ZERO
        );
        return meshdefinition;
    }


    @Override
    public void setupAnim(T entity,
                          float limbSwing,
                          float limbSwingAmount,
                          float ageInTicks,
                          float netHeadYaw,
                          float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var vrPlayer = VRClientPlayers
                .getPlayer(entity.getUUID());

        if (vrPlayer == null) {
            return;
        }
        var renderPose = vrPlayer.getPoseData(PlayerPoseType.RENDER);
        var hmdDir = renderPose.getHmd().getDirection();
        float f1 = (float) Mth.atan2(
                -hmdDir.x(),
                -hmdDir.z()
        );
        float f2 = (float) Math.asin(
                hmdDir.y()
                        / hmdDir.length()
        );
        double d1 = renderPose.getBodyYaw();
        this.head.xRot = -f2;
        this.head.yRot = (float) (Math.PI - (double) f1 - d1);
        this.laying = this.swimAmount > 0.0F
                || entity.isFallFlying()
                && !entity.isAutoSpinAttack();

        if (this.laying) {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = -4.0F;
            this.head.xRot = (float) ((double) this.head.xRot - (Math.PI / 2D));
        } else if (this.crouching) {
            // move head down when crouching
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 4.2f;
        } else {
            this.head.z = 0.0F;
            this.head.x = 0.0F;
            this.head.y = 0.0F;
        }

        this.vrHMD.visible = true;

        this.vrHMD.copyFrom(this.head);
        this.hat.copyFrom(this.head);
    }


    public void renderHMD(PoseStack poseStack,
                          VertexConsumer vertexConsumer,
                          int i, int noOverlay) {
        this.vrHMD.render(poseStack, vertexConsumer, i, noOverlay);
    }
}

package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class CenteredArmsPlayerMesh {

    private CenteredArmsPlayerMesh() {}

    public static MeshDefinition create(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition root = mesh.getRoot();

        float boxXSize = slim ? 3.0F : 4.0F;
        float halfWidth = boxXSize / 2.0F;
        float boxYSize = 12.0F;
        float boxZSize = 4.0F;
        float addBoxY = -2.0F;
        float addBoxZ = -2.0F;
        float pivotY = slim ? 2.5F : 2.0F;
        float pivotX = 5.0F + (halfWidth - 1.0F);
        float sleeveExtend = 0.25F;

        // ---- Left arm ----
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize, cubeDeformation),
                PartPose.offset(pivotX, pivotY, 0.0F));
        root.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(48, 48)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize,
                                cubeDeformation.extend(sleeveExtend)),
                PartPose.offset(pivotX, pivotY, 0.0F));

        // ---- Right arm ----
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize, cubeDeformation),
                PartPose.offset(-pivotX, pivotY, 0.0F));
        root.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(40, 32)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize,
                                cubeDeformation.extend(sleeveExtend)),
                PartPose.offset(-pivotX, pivotY, 0.0F));

        return mesh;
    }

    public static float armPivotX(boolean slim, boolean leftArm) {
        float halfWidth = slim ? 1.5F : 2.0F;
        float x = 5.0F + (halfWidth - 1.0F);
        return leftArm ? x : -x;
    }

    public static float armPivotY(boolean slim) {
        return slim ? 2.5F : 2.0F;
    }
}
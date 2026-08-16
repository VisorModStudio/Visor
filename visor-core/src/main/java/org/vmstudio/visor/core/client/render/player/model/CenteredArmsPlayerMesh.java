package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class CenteredArmsPlayerMesh {

    /**
     * 1.21.2 moved the slim arm pivot from 2.5 up to 2.0, so slim and classic arms now
     * share the vanilla pivot - keeping the old 2.5 leaves slim arms half a unit low.
     */
    private static final float ARM_PIVOT_Y = 2.0F;

    private CenteredArmsPlayerMesh() {}

    /**
     * Rebuilds the player mesh with the arm boxes centred on their pivot, so a VR arm can be
     * aimed straight down the controller direction without the vanilla shoulder offset.
     * <p>
     * 1.21.2 reparented the overlay parts: {@code left_sleeve}/{@code right_sleeve} are children
     * of the arms at {@link PartPose#ZERO} (pants under the legs, jacket under the body, hat
     * under the head), and {@code Model#renderToBuffer} now renders the whole root tree instead
     * of the explicit {@code headParts()}/{@code bodyParts()} lists. Registering the centred
     * sleeves on the root - as the pre-1.21.2 mesh did - therefore both leaves
     * {@code PlayerModel#leftSleeve} pointing at vanilla's off-centre child cube and draws the
     * centred cube a second time as an unparented part floating at the shoulder.
     */
    public static MeshDefinition create(CubeDeformation cubeDeformation, boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(cubeDeformation, slim);
        PartDefinition root = mesh.getRoot();

        float boxXSize = slim ? 3.0F : 4.0F;
        float halfWidth = boxXSize / 2.0F;
        float boxYSize = 12.0F;
        float boxZSize = 4.0F;
        float addBoxY = -2.0F;
        float addBoxZ = -2.0F;
        float pivotY = ARM_PIVOT_Y;
        float pivotX = 5.0F + (halfWidth - 1.0F);
        float sleeveExtend = slim ? 0.12F : 0.25F;

        // ---- Left arm ----
        // addOrReplaceChild carries the replaced definition's children over, so the vanilla
        // off-centre sleeve comes along and has to be replaced with the centred one.
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize, cubeDeformation),
                PartPose.offset(pivotX, pivotY, 0.0F));
        leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create()
                        .texOffs(48, 48)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize,
                                cubeDeformation.extend(sleeveExtend)),
                PartPose.ZERO);

        // ---- Right arm ----
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize, cubeDeformation),
                PartPose.offset(-pivotX, pivotY, 0.0F));
        rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create()
                        .texOffs(40, 32)
                        .addBox(-halfWidth, addBoxY, addBoxZ, boxXSize, boxYSize, boxZSize,
                                cubeDeformation.extend(sleeveExtend)),
                PartPose.ZERO);

        return mesh;
    }

    public static float armPivotX(boolean slim, boolean leftArm) {
        float halfWidth = slim ? 1.5F : 2.0F;
        float x = 5.0F + (halfWidth - 1.0F);
        return leftArm ? x : -x;
    }

    public static float armPivotY(boolean slim) {
        return ARM_PIVOT_Y;
    }
}
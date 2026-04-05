package org.vmstudio.visor.core.client.render.player.model.full.armor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.full.VRPlayerModelFull;
import org.vmstudio.visor.core.client.render.player.model.HandModel;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.utils.ModelUtils;

public class VRArmorModelFull<T extends LivingEntity> extends HumanoidArmorModel<T> implements HandModel {

    public final ModelPart leftHand;
    public final ModelPart rightHand;

    public VRArmorModelFull(ModelPart root) {
        super(root);
        this.leftHand = root.getChild("left_hand");
        this.rightHand = root.getChild("right_hand");
        ModelUtils.copyTextures(this.leftArm, this.leftHand);
        ModelUtils.copyTextures(this.rightArm, this.rightHand);
    }

    public static MeshDefinition createBodyLayer(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = HumanoidArmorModel.createBodyLayer(cubeDeformation);
        PartDefinition partDefinition = meshDefinition.getRoot();

        int upperExtension = VRPlayerModelFull.UPPER_EXTENSION;
        int lowerExtension = VRPlayerModelFull.LOWER_EXTENSION;
        float lowerShrinkage = -0.05F;

        partDefinition.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(40, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-5.0F, 2.5F, 0.0F));
        partDefinition.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(40, 23 - lowerExtension)
                .addBox(-2.0F, -5.0F - lowerExtension, -2.0F, 4.0F, 5.0F + lowerExtension, 4.0F,
                    cubeDeformation.extend(lowerShrinkage)),
            PartPose.offset(-5.0F, 2.5F, 0.0F));


        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).mirror()
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 5.0F + upperExtension, 4.0F, cubeDeformation),
            PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        return meshDefinition;
    }


    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftHand, this.rightHand));
    }

    @Override
    public ModelPart getLeftHand() {
        return this.leftHand;
    }

    @Override
    public ModelPart getRightHand() {
        return this.rightHand;
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        this.leftHand.visible = visible;
        this.rightHand.visible = visible;
    }
}

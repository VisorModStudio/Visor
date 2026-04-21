package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

public final class ItemHandTransformHelper {
    private ItemHandTransformHelper() {}

    public static void applyArmScaleCorrection(PoseStack poseStack) {
        float armsScale = VRClientSettings.getPlayerModelArmsScale(); // I don't know when we can use it
        poseStack.translate(0.0F, 0.65F, 0.0F);
//        poseStack.scale(1.0F, armsScale, 1.0F);
        poseStack.translate(0.0F, -0.65F, 0.0F);
    }

    public static void applyVanillaThirdPersonItemTransform(PoseStack poseStack, HumanoidArm arm) {
        boolean isLeftHand = arm == HumanoidArm.LEFT;
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate((isLeftHand ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
    }
}
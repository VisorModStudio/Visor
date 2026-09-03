package org.vmstudio.visor.core.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.ClientContext;

public class PlayerModelUtils {
    // vanilla LivingEntityRenderer scales the player model down by this factor
    private static final float VANILLA_MODEL_SCALE = 0.9375F;
    // vanilla's y offset that puts the flipped model back on the ground
    private static final float VANILLA_Y_LIFT = 1.501F;

    private PlayerModelUtils() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static Vector3f toWorldSpace(VRClientPlayer vrPlayer, Vector3fc modelPos,
                                        float bodyYaw, Vector3f dest) {
        float modelScale = VANILLA_MODEL_SCALE * vrPlayer.getModelScale();
        float blocksPerUnit = modelScale / 16.0F;
        dest.set(modelPos)
                .mul(-blocksPerUnit, -blocksPerUnit, blocksPerUnit)
                .rotateY(Mth.PI - bodyYaw);
        dest.y += VANILLA_Y_LIFT * modelScale;
        return dest;
    }

    public static Vector3f getModelOrigin(@NotNull LivingEntity entity) {
        float partialTicks = ClientContext.visor.getPartialTicks();
        return new Vector3f(
                (float) Mth.lerp(partialTicks, entity.xo, entity.getX()),
                (float) Mth.lerp(partialTicks, entity.yo, entity.getY()),
                (float) Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
    }

    public static void controllerToModelOrientation(PoseStack poseStack) {
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }
}

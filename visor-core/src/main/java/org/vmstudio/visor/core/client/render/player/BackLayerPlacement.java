package org.vmstudio.visor.core.client.render.player;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.utils.PlayerModelUtils;

public final class BackLayerPlacement {
    // model's default back attachment sits this far up from the part origin
    private static final float ATTACH_HEIGHT = 24F;

    private final Matrix3f torsoRotation = new Matrix3f();
    private final Vector3f probe = new Vector3f();

    private float pitch;
    private float yaw;

    public void aim(ModelPart body, boolean unwrapPitch) {
        torsoRotation.rotationZ(body.zRot).rotateY(-body.yRot).rotateX(-body.xRot);
        torsoRotation.transform(VRMathUtils.UP_VECTOR, probe);
        float measuredPitch = (float) Math.atan2(probe.y, probe.z) - Mth.HALF_PI;
        if (unwrapPitch && measuredPitch < -Mth.PI) {
            measuredPitch += Mth.TWO_PI;
        }
        this.pitch = measuredPitch;

        torsoRotation.transform(VRMathUtils.RIGHT_VECTOR, probe);
        this.yaw = (float) -Math.atan2(probe.x, probe.y) + Mth.HALF_PI;
    }

    public float pitch() { return pitch; }

    public float yaw() { return yaw; }

    public static float restingDepth(ModelPart body) {
        return 2F - body.xRot / Mth.PI;
    }

    public Vector3f place(VRClientPlayer vrPlayer, ModelPart body, Vector3f localOffset, Vector3f dest) {
        localOffset.rotateX(pitch);
        localOffset.rotateZ(yaw);
        localOffset.add(body.x, body.y + ATTACH_HEIGHT, body.z);
        return PlayerModelUtils.toWorldSpace(vrPlayer, localOffset, 0F, dest);
    }
}

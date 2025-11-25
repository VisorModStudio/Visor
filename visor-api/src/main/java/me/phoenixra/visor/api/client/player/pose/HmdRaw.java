package me.phoenixra.visor.api.client.player.pose;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.common.utils.QuaternionFloatHistory;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public interface HmdRaw {


    @NotNull
    Vector3fHistory getPositionHistory();
    @NotNull
    Vector3fHistory getPivotHistory();
    @NotNull
    QuaternionFloatHistory getRotationHistory();


    Matrix4fc getDevicePose();

    Matrix4fc getRotation();


    Vector3f getHeadsetPosition();

    Vector3f getEyePosition(EyeType eye);

    Matrix4fc getEyeRotation(EyeType eye);

    default Vector3f getVector() {
        return getRotation()
                .transformDirection(VRMathUtils.BACK_VECTOR, new Vector3f());
    }
}

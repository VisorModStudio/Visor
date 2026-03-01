package org.vmstudio.visor.api.client.player.pose;

import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.common.utils.QuaternionFloatHistory;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Holds pose data received from VR about HMD
 * Updated every frame, at the beginning of game loop
 * <p>
 *     Useful when you need to use most recent data directly from VR
 * </p>
 */
public interface RawHmd {


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

    Vector3f getVector();
}

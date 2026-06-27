package org.vmstudio.visor.api.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.utils.QuaternionFloatHistory;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;

public interface RawTracker {

    boolean isTracking();
    VRBodyPartType getType();

    Matrix4fc getPose();
    Matrix4fc getRotation();

    Vector3f getPosition();
    Vector3f getVector();

    @NotNull
    Vector3fHistory getPositionHistory();
    @NotNull
    QuaternionFloatHistory getRotationHistory();
}

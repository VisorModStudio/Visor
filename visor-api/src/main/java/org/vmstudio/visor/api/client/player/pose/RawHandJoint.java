package org.vmstudio.visor.api.client.player.pose;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.player.VRHandJointType;

public interface RawHandJoint {

    boolean isTracking();
    VRHandJointType getType();

    Matrix4fc getPose();
    Matrix4fc getRotation();

    Vector3f getPosition();
    Vector3f getVector();

    float getRadius();
}

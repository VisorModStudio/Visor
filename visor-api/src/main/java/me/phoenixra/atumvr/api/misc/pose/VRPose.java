package me.phoenixra.atumvr.api.misc.pose;

import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface VRPose {

    Matrix4fc matrix();

    Quaternionfc orientation();

    Vector3fc position();

}

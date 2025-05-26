package me.phoenixra.atumvr.api.misc.pose;


import org.joml.*;

public record VRPoseRecord(Matrix4fc matrix, Quaternionfc orientation, Vector3fc position) implements VRPose{

    public static VRPoseRecord EMPTY = new VRPoseRecord(new Matrix4f(), new Quaternionf(), new Vector3f());
}

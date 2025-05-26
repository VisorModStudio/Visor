package me.phoenixra.visor.core.client.data;



import me.phoenixra.visor.core.client.data.raw.RawHmdPose;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import me.phoenixra.visor.core.client.ClientContext;

//Helper to optimize some logic
public class VRPoseHelper {


    public static @NotNull Vec3 getHeadPivot(Vec3 origin,
                                                float walkMul,
                                                float worldScale,
                                                float rotation) {
        VRPoseElement hmd = createHmdPose(origin,walkMul,worldScale,rotation);
        Vec3 hmdPosition = hmd.getPosition();
        Vector3f transform = hmd.getRotationMatrix()
                .transformDirection(
                        new Vector3f(
                                0.0F,
                                -0.1F * worldScale,
                                0.1F * worldScale
                        )
                );
        return new Vec3(
                (double) transform.x() + hmdPosition.x,
                (double) transform.y() + hmdPosition.y,
                (double) transform.z() + hmdPosition.z
        );
    }

    public static VRPoseElement createHmdPose(Vec3 origin,
                                              float walkMul,
                                              float worldScale,
                                              float rotation) {
        RawHmdPose hmdData = ClientContext.rawPlayerPose.getHmdData();
        Vec3[] centers = computeCenterPositions(hmdData, walkMul);
        Vec3 centerPosition = centers[1];
        return new VRPoseElement(
                origin,
                rotation,
                worldScale,
                hmdData.getRotation(),
                centerPosition,
                hmdData.getVector()
        );
    }


    private static Vec3[] computeCenterPositions(RawHmdPose hmdData,
                                                 float walkMul) {
        Vec3 centerEyePosition = hmdData.getCenterEyePosition();
        Vec3 centerPosition = new Vec3(
                centerEyePosition.x * walkMul,
                centerEyePosition.y,
                centerEyePosition.z * walkMul
        );
        return new Vec3[]{ centerEyePosition, centerPosition };
    }

}

package me.phoenixra.visor.core.client.data;



import me.phoenixra.visor.core.client.data.raw.RawHmd;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Vector3fc;

//Helper to optimize some logic
public class PoseDataHelper {


    public static @NotNull Vector3f getHeadPivot(Vector3fc origin,
                                                 float walkMul,
                                                 float worldScale,
                                                 float rotation) {
        PoseElementImpl hmd = createHmdPose(origin,walkMul,worldScale,rotation);
        var hmdPosition = hmd.getPosition();
        Vector3f transform = hmd.getRotation()
                .transformDirection(
                        new Vector3f(
                                0.0F,
                                -0.1F * worldScale,
                                0.1F * worldScale
                        )
                );
        return new Vector3f(
                transform.x() + hmdPosition.x(),
                transform.y() + hmdPosition.y(),
                transform.z() + hmdPosition.z()
        );
    }

    public static PoseElementImpl createHmdPose(Vector3fc origin,
                                                float walkMul,
                                                float worldScale,
                                                float rotation) {
        RawHmd hmdData = ClientContext.rawPoseHandler.getHmdData();
        Vector3fc[] centers = computeCenterPositions(hmdData, walkMul);
        var centerPosition = centers[1];
        return new PoseElementImpl(
                origin,
                rotation,
                worldScale,
                hmdData.getRotation(),
                centerPosition,
                hmdData.getVector()
        );
    }


    private static Vector3fc[] computeCenterPositions(RawHmd hmdData,
                                                     float walkMul) {
        var headsetPosition = hmdData.getHeadsetPosition();
        var headsetPosFinal = new Vector3f(
                headsetPosition.x * walkMul,
                headsetPosition.y,
                headsetPosition.z * walkMul
        );
        return new Vector3fc[]{ headsetPosition, headsetPosFinal };
    }

}

package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public abstract class RawPoseHandler {

    @Getter
    protected RawHmdImpl hmdData;

    protected RawControllerImpl controllerLeftData;
    protected RawControllerImpl controllerRightData;

    @Getter
    protected float gunAngle = 0.0f;



    public RawPoseHandler() {
        this.hmdData = new RawHmdImpl();
        this.controllerLeftData = new RawControllerImpl();
        this.controllerRightData = new RawControllerImpl();

    }


    public abstract void updatePose();



    public Matrix4f getSmoothedRotation(HandType controller, float lenSec) {
        RawControllerImpl controllerData = getControllerData(controller);

        Vector3f averagePosForward = controllerData.getForwardHistory().averagePosition(lenSec);
        Vector3f averagePosUp = controllerData.getUpHistory().averagePosition(lenSec);
        Vector3f cross = averagePosForward.cross(averagePosUp);
        return new Matrix4f(
                cross.x, averagePosForward.x, averagePosUp.x, 0,
                cross.y, averagePosForward.y, averagePosUp.y, 0,
                cross.z, averagePosForward.z, averagePosUp.z, 0,
                0,0,0, 1
        );
    }


    public RawControllerImpl getControllerData(HandType controller) {
        if (controller.asControllerType(VRClientSettings.isLeftHanded()) == ControllerType.LEFT){
            return controllerLeftData;
        }
        return controllerRightData;

    }
}

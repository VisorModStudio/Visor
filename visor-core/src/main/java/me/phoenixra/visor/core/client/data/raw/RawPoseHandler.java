package me.phoenixra.visor.core.client.data.raw;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public abstract class RawPoseHandler {

    @Getter
    protected RawHmd hmdData;

    protected RawController controllerLeftData;
    protected RawController controllerRightData;

    @Getter
    protected float gunAngle = 0.0f;



    public RawPoseHandler() {
        this.hmdData = new RawHmd();
        this.controllerLeftData = new RawController();
        this.controllerRightData = new RawController();

    }


    public abstract void updatePose();



    public Matrix4f getSmoothedRotation(ControllerHand controller, float lenSec) {
        RawController controllerData = getControllerData(controller);

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


    public RawController getControllerData(ControllerHand controller) {
        if (controller.getType(VRClientSettings.isLeftHanded()) == ControllerType.LEFT){
            return controllerLeftData;
        }
        return controllerRightData;

    }
}

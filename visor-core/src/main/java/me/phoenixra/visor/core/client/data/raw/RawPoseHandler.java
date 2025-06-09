package me.phoenixra.visor.core.client.data.raw;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;


public abstract class RawPoseHandler {

    @Getter
    protected RawHmd hmdData;

    protected RawController controllerLeftData;
    protected RawController controllerRightData;

    @Getter
    protected double gunAngle = 0.0D;



    public RawPoseHandler() {
        this.hmdData = new RawHmd();
        this.controllerLeftData = new RawController();
        this.controllerRightData = new RawController();

    }


    public abstract void updatePose();



    public Matrix4f getSmoothedRotation(ControllerHand controller, float lenSec) {
        RawController controllerData = getControllerData(controller);

        Vec3 averagePosForward = controllerData.getForwardHistory().averagePosition(lenSec);
        Vec3 averagePosUp = controllerData.getUpHistory().averagePosition(lenSec);
        Vec3 cross = averagePosForward.cross(averagePosUp);
        return new Matrix4f(
                (float) cross.x, (float) averagePosForward.x, (float) averagePosUp.x, 0,
                (float) cross.y, (float) averagePosForward.y, (float) averagePosUp.y, 0,
                (float) cross.z, (float) averagePosForward.z, (float) averagePosUp.z, 0,
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

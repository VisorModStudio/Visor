package me.phoenixra.visor.core.client.data.raw;

import lombok.Getter;
import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Getter
public abstract class RawPlayerPose {

    protected RawHmdPose hmdData;
    protected RawControllerPose controllerLeftData;
    protected RawControllerPose controllerRightData;

    @Getter
    protected double gunAngle = 0.0D;



    public RawPlayerPose() {
        this.hmdData = new RawHmdPose();
        this.controllerLeftData = new RawControllerPose();
        this.controllerRightData = new RawControllerPose();

    }


    public abstract void updatePose();



    public Matrix4f getSmoothedRotation(ControllerHand controller, float lenSec) {
        RawControllerPose controllerData = getControllerData(controller);

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


    public RawControllerPose getControllerData(ControllerHand controller) {
        if (controller == ControllerHand.OFFHAND) return controllerLeftData;
        return controllerRightData;

    }
    public RawControllerPose getControllerData(int type) {
        if (type == 0) return controllerRightData;
        return controllerLeftData;
    }
}

package org.vmstudio.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceController;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceHMD;
import me.phoenixra.atumvr.core.input.device.XRDeviceController;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.pose.raw.RawPoseHandler;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.vmstudio.visor.core.client.ClientContext;

public class XrRawPoseHandler extends RawPoseHandler {
    private final XrProvider provider;
    public XrRawPoseHandler(XrProvider provider){
        this.provider = provider;
    }

    @Override
    public void updatePose() {
        //HND
        var hmdDevice = provider.getInputHandler().getDevice(
                AtumVRDeviceHMD.ID, XRDeviceHMD.class
        );
        hmdData.setTracking(hmdDevice.isActive());

        hmdData.getDevicePoseMutable().set(hmdDevice.getPose().matrix());
        hmdData.getRotationMutable().set(hmdDevice.getPose().orientation());
        hmdData.getLeftEyePoseMutable()
                .set(hmdDevice.getEyePose(EyeType.LEFT).matrix());
        hmdData.getRightEyePoseMutable()
                .set(hmdDevice.getEyePose(EyeType.RIGHT).matrix());


        Matrix4f hmdRotation = hmdData.getRotationMutable();
        Matrix4f hmdPose = hmdData.getDevicePoseMutable();
        hmdRotation.set3x3(hmdPose);

        Vector3f headsetPos = hmdData.getHeadsetPosition();
        hmdData.getPositionHistory().add(headsetPos);
        Vector3f vector3 = hmdData.getRotation()
                .transformDirection(new Vector3f(0.0F, -0.1F, 0.1F));
        hmdData.getPivotHistory()
                .add(new Vector3f(
                                vector3.x() + headsetPos.x,
                                vector3.y() + headsetPos.y,
                                vector3.z() + headsetPos.z
                        )
                );
        hmdData.getRotationHistory()
                .add(new Quaternionf().setFromNormalized(hmdRotation)
                        .rotateY(ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getRotationY()));


        //LEFT CONTROLLER

        var controllerLeftDevice = provider.getInputHandler().getDevice(
                AtumVRDeviceController.ID_LEFT, XRDeviceController.class
        );
        controllerLeftData.setTracking(controllerLeftDevice.isActive());

        //---Aim
        controllerLeftData.getAimPoseMutable().set(
                controllerLeftDevice.getPose().matrix()
        );
        controllerLeftData.getAimRotationMutable().set(
                controllerLeftDevice.getPose().orientation()
        );

        //---Grip
        controllerLeftData.getGripPoseMutable().set(
                controllerLeftDevice.getGripPose().matrix()
        );
        controllerLeftData.getGripRotationMutable().set(
                controllerLeftDevice.getGripPose().orientation()
        );

        //---History
        controllerLeftData.getPositionHistory().add(
                controllerLeftData.getAimPosition()
        );

        controllerLeftData.getForwardHistory().add(
                controllerLeftData.getAimVector()
        );
        Vector3f upVec =  controllerLeftDevice.getPose().orientation()
                .transform(VRMathUtils.UP_VECTOR, new Vector3f());
        controllerLeftData.getUpHistory().add(upVec);


        //RIGHT CONTROLLER
        var controllerRightDevice = provider.getInputHandler().getDevice(
                AtumVRDeviceController.ID_RIGHT, XRDeviceController.class
        );
        controllerRightData.setTracking(controllerRightDevice.isActive());

        //---Aim
        controllerRightData.getAimPoseMutable().set(
                controllerRightDevice.getPose().matrix()
        );
        controllerRightData.getAimRotationMutable().set(
                controllerRightDevice.getPose().orientation()
        );

        //---Grip
        controllerRightData.getGripPoseMutable().set(
                controllerRightDevice.getGripPose().matrix()
        );
        controllerRightData.getGripRotationMutable().set(
                controllerRightDevice.getGripPose().orientation()
        );

        //---History
        controllerRightData.getPositionHistory().add(
                controllerRightData.getAimPosition()
        );

        controllerRightData.getForwardHistory().add(
                controllerRightData.getAimVector()
        );
        upVec =  controllerRightDevice.getPose().orientation()
                .transform(VRMathUtils.UP_VECTOR, new Vector3f());

        controllerRightData.getUpHistory().add(upVec);


        var aimVector = controllerLeftData.getAimVector().normalize(new Vector3f());
        var gripVector = controllerLeftData.getGripVector().normalize(new Vector3f());

        this.gunAngle = (float) Math.toDegrees(
                Math.acos(
                        Math.abs(
                                aimVector.dot(gripVector)
                        )
                )
        );
    }
}

package me.phoenixra.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.VRDeviceController;
import me.phoenixra.atumvr.api.input.device.VRDeviceHMD;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceController;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceHMD;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.data.raw.RawPoseHandler;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import me.phoenixra.visor.core.client.ClientContext;

public class XrRawPoseHandler extends RawPoseHandler {
    private final XrVRProvider provider;
    public XrRawPoseHandler(XrVRProvider provider){
        this.provider = provider;
    }

    @Override
    public void updatePose() {
        //HND
        var hmdDevice = provider.getInputHandler().getDevice(
                VRDeviceHMD.ID, OpenXRDeviceHMD.class
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

        Vec3 headsetPos = hmdData.getHeadsetPosition();
        hmdData.getPositionHistory().add(headsetPos);
        Vector3f vector3 = hmdData.getRotation()
                .transformDirection(new Vector3f(0.0F, -0.1F, 0.1F));
        hmdData.getPivotHistory()
                .add(new Vec3(
                                (double) vector3.x() + headsetPos.x,
                                (double) vector3.y() + headsetPos.y,
                                (double) vector3.z() + headsetPos.z
                        )
                );
        hmdData.getRotationHistory()
                .add(new Quaternionf().setFromNormalized(hmdRotation)
                        .rotateY(ClientContext.player.getRotationYaw()));


        //LEFT CONTROLLER

        var controllerLeftDevice = provider.getInputHandler().getDevice(
                VRDeviceController.ID_LEFT, OpenXRDeviceController.class
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
        Vec3 upVec =  VRMathUtils.convertToMcVector(
                controllerLeftDevice.getPose().orientation()
                .transform(VRMathUtils.upVector, new Vector3f())
        );
        controllerLeftData.getUpHistory().add(upVec);


        //RIGHT CONTROLLER
        var controllerRightDevice = provider.getInputHandler().getDevice(
                VRDeviceController.ID_RIGHT, OpenXRDeviceController.class
        );
        controllerRightData.setTracking(controllerLeftDevice.isActive());

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
        upVec =  VRMathUtils.convertToMcVector(
                controllerRightDevice.getPose().orientation()
                        .transform(VRMathUtils.upVector, new Vector3f())
        );
        controllerRightData.getUpHistory().add(upVec);


    }
}

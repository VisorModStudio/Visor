package org.vmstudio.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.body.AtumVRBodyJoint;
import me.phoenixra.atumvr.api.input.body.hand.AtumVRHandJoint;
import me.phoenixra.atumvr.api.input.body.hand.AtumVRHandView;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceController;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceHMD;
import me.phoenixra.atumvr.api.misc.pose.AtumVRPose;
import me.phoenixra.atumvr.core.input.device.XRDeviceController;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.player.VRHandDataSource;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.pose.raw.RawPoseHandler;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.raw.RawHandImpl;
import org.vmstudio.visor.core.client.player.pose.raw.RawTrackerImpl;

public class XrRawPoseHandler extends RawPoseHandler {

    private static final VRBodyPartType[] BODY_PARTS = VRBodyPartType.values();

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

        Vector3f headsetPos = hmdData.getPosition();
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


        //TRACKERS
        if(trackersData.isTracking()){
            var body = provider.getInputHandler().getVRBody();
            for(VRBodyPartType part : BODY_PARTS){
                AtumVRBodyJoint joint = toBodyJoint(part);
                if(joint == null){
                    continue;
                }
                updateTracker(trackersData.getTracker(part), body.getJointPose(joint));
            }
        }

        //HANDS
        if(handsData.isTracking()){
            var hands = provider.getInputHandler().getVRHands();
            updateHand(handsData.getLeftHand(), hands.getLeftHand());
            updateHand(handsData.getRightHand(), hands.getRightHand());
        }
    }

    private static AtumVRBodyJoint toBodyJoint(VRBodyPartType part){
        return switch (part){
            case WAIST -> AtumVRBodyJoint.WAIST;
            case CHEST -> AtumVRBodyJoint.CHEST;
            case LEFT_FOOT -> AtumVRBodyJoint.LEFT_FOOT;
            case RIGHT_FOOT -> AtumVRBodyJoint.RIGHT_FOOT;
            case LEFT_ANKLE -> AtumVRBodyJoint.LEFT_ANKLE;
            case RIGHT_ANKLE -> AtumVRBodyJoint.RIGHT_ANKLE;
            case LEFT_KNEE -> AtumVRBodyJoint.LEFT_KNEE;
            case RIGHT_KNEE -> AtumVRBodyJoint.RIGHT_KNEE;
            case LEFT_WRIST -> AtumVRBodyJoint.LEFT_WRIST;
            case RIGHT_WRIST -> AtumVRBodyJoint.RIGHT_WRIST;
            case LEFT_ELBOW -> AtumVRBodyJoint.LEFT_ELBOW;
            case RIGHT_ELBOW -> AtumVRBodyJoint.RIGHT_ELBOW;
            case LEFT_SHOULDER -> AtumVRBodyJoint.LEFT_SHOULDER;
            case RIGHT_SHOULDER -> AtumVRBodyJoint.RIGHT_SHOULDER;
            //delivered by the HMD/controller devices, not trackers
            case HEAD, MAIN_HAND, OFFHAND -> null;
        };
    }

    private void updateHand(RawHandImpl handData, AtumVRHandView handView){
        if(!handView.isTracked()){
            handData.setTracking(false);
            handData.setDataSource(VRHandDataSource.UNKNOWN);
            for(int i = 0; i < VRHandJointType.COUNT; i++){
                handData.getJoint(VRHandJointType.fromIndex(i)).setTracking(false);
            }
            return;
        }
        handData.setDataSource(toDataSource(handView.getDataSource()));

        for(int i = 0; i < VRHandJointType.COUNT; i++){
            var jointData = handData.getJoint(VRHandJointType.fromIndex(i));
            AtumVRHandJoint joint = AtumVRHandJoint.fromIndex(i);
            AtumVRPose jointPose = handView.getJointPose(joint);
            if(jointPose == null){
                jointData.setTracking(false);
                continue;
            }
            jointData.getDevicePoseMutable().set(jointPose.matrix());
            jointData.getRotationMutable().set(jointPose.orientation());
            jointData.getRotationMutable().set3x3(jointData.getDevicePoseMutable());
            jointData.setRadius(handView.getJointRadius(joint));
            jointData.setTracking(true);
        }
        handData.setTracking(true);
    }

    private static VRHandDataSource toDataSource(AtumVRHandView.DataSource source){
        return switch (source){
            case HAND -> VRHandDataSource.HAND;
            case CONTROLLER -> VRHandDataSource.CONTROLLER;
            case UNKNOWN -> VRHandDataSource.UNKNOWN;
        };
    }

    private void updateTracker(RawTrackerImpl tracker, AtumVRPose jointPose){
        if(jointPose == null){
            tracker.setTracking(false);
            return;
        }
        tracker.getDevicePoseMutable().set(jointPose.matrix());
        tracker.getRotationMutable().set(jointPose.orientation());

        Matrix4f trackerPose = tracker.getDevicePoseMutable();
        Matrix4f trackerRotation = tracker.getRotationMutable();
        trackerRotation.set3x3(trackerPose);

        Vector3f trackerPos = tracker.getPosition();
        tracker.getPositionHistory()
                .add(trackerPos);
        tracker.getRotationHistory()
                .add(new Quaternionf().setFromNormalized(trackerRotation)
                        .rotateY(ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getRotationY()));
        tracker.setTracking(true);
    }
}

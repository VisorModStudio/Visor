package org.vmstudio.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceController;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceHMD;
import me.phoenixra.atumvr.api.input.profile.tracker.ViveTrackerRole;
import me.phoenixra.atumvr.core.input.device.XRDeviceController;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import me.phoenixra.atumvr.core.input.device.XRDeviceViveTracker;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.pose.raw.RawPoseHandler;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.raw.RawTrackerImpl;

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
        var trackersManager = provider.getInputHandler().getTrackerManager();

        if(!trackersData.isTracking()){
            return;
        }
        // ---- WAIST
        var trackerDevice = trackersManager.getDevicesMap()
                .get(ViveTrackerRole.WAIST);
        var tracker = trackersData.getWaist();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- CHEST
        trackerDevice = trackersManager.getDevicesMap()
                .get(ViveTrackerRole.CHEST);
        tracker = trackersData.getChest();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT FOOT
        trackerDevice = trackersManager.getDevicesMap()
                .get(ViveTrackerRole.LEFT_FOOT);
        tracker = trackersData.getLeftFoot();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT FOOT
        trackerDevice = trackersManager.getDevicesMap()
                .get(ViveTrackerRole.RIGHT_FOOT);
        tracker = trackersData.getRightFoot();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT ANKLE
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.LEFT_ANKLE);
        tracker = trackersData.getLeftAnkle();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT ANKLE
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.RIGHT_ANKLE);
        tracker = trackersData.getRightAnkle();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT KNEE
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.LEFT_KNEE);
        tracker = trackersData.getLeftKnee();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT KNEE
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.RIGHT_KNEE);
        tracker = trackersData.getRightKnee();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT WRIST
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.LEFT_WRIST);
        tracker = trackersData.getLeftWrist();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT WRIST
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.RIGHT_WRIST);
        tracker = trackersData.getRightWrist();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT ELBOW
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.LEFT_ELBOW);
        tracker = trackersData.getLeftElbow();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT ELBOW
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.RIGHT_ELBOW);
        tracker = trackersData.getRightElbow();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- LEFT SHOULDER
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.LEFT_SHOULDER);
        tracker = trackersData.getLeftShoulder();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }
        // ---- RIGHT SHOULDER
        trackerDevice = trackersManager.getDevicesMap().get(ViveTrackerRole.RIGHT_SHOULDER);
        tracker = trackersData.getRightShoulder();
        if(trackerDevice == null){
            tracker.setTracking(false);
        }else{
            updateTracker(tracker, trackerDevice);
            tracker.setTracking(trackerDevice.isActive());
        }


    }

    private void updateTracker(RawTrackerImpl tracker,
                               XRDeviceViveTracker trackerDevice){
        tracker.getDevicePoseMutable().set(trackerDevice.getPose().matrix());
        tracker.getRotationMutable().set(trackerDevice.getPose().orientation());

        Matrix4f trackerPose = tracker.getDevicePoseMutable();
        Matrix4f trackerRotation = tracker.getRotationMutable();
        trackerRotation.set3x3(trackerPose);

        Vector3f trackerPos = tracker.getPosition();
        tracker.getPositionHistory()
                .add(trackerPos);
        tracker.getRotationHistory()
                .add(new Quaternionf().setFromNormalized(trackerRotation)
                        .rotateY(ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getRotationY()));

    }
}

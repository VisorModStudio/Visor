package org.vmstudio.visor.core.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseHand;
import org.vmstudio.visor.api.common.player.VRPoseHands;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.raw.RawHandImpl;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.ArrayList;
import java.util.List;

public class LocalHandsPose implements VRPoseHands {

    private final LocalPlayerPose owner;

    private final LocalHandPose leftHand;
    private final LocalHandPose rightHand;


    public LocalHandsPose(@NotNull LocalPlayerPose owner){
        this.owner = owner;

        var handsData = ClientContext.rawPoseHandler.getHandsData();
        this.leftHand = new LocalHandPose(handsData.getLeftHand());
        this.rightHand = new LocalHandPose(handsData.getRightHand());

        leftHand.updateActiveJoints();
        rightHand.updateActiveJoints();
    }

    public void updateTracking(Vector3fc origin,
                               float worldScale,
                               float rotationY){
        boolean changed = leftHand.updateActiveJoints();
        changed |= rightHand.updateActiveJoints();
        if(changed){
            owner.resetPoseElements();
        }

        leftHand.updateTracking(origin, worldScale, rotationY);
        rightHand.updateTracking(origin, worldScale, rotationY);
    }

    public void copyFrom(LocalHandsPose other){
        leftHand.copyFrom(other.leftHand);
        rightHand.copyFrom(other.rightHand);
    }

    public List<VRPose> getActiveJointsPose(){
        List<VRPose> out = new ArrayList<>(leftHand.getActiveJointsPose());
        out.addAll(rightHand.getActiveJointsPose());
        return out;
    }


    @Override
    public boolean isActive() {
        return ClientContext.rawPoseHandler.getHandsData().isTracking();
    }

    @Override
    public @NotNull LocalHandPose getLeftHand() {
        return leftHand;
    }

    @Override
    public @NotNull LocalHandPose getRightHand() {
        return rightHand;
    }


    public static class LocalHandPose implements VRPoseHand {

        private final RawHandImpl handData;
        private final VRPoseImpl[] joints = new VRPoseImpl[VRHandJointType.COUNT];

        private List<VRPose> activeJointsPose = new ArrayList<>();
        private final List<VRHandJointType> activeJointsType = new ArrayList<>();

        private LocalHandPose(@NotNull RawHandImpl handData){
            this.handData = handData;
            for (int i = 0; i < joints.length; i++) {
                joints[i] = new VRPoseImpl();
            }
        }

        private boolean updateActiveJoints(){
            List<VRPose> newJoints = new ArrayList<>();

            activeJointsType.clear();

            if(!handData.isTracking()){
                if(!activeJointsPose.isEmpty()){
                    activeJointsPose = newJoints;
                    return true;
                }
                return false;
            }

            for (int i = 0; i < joints.length; i++) {
                VRHandJointType type = VRHandJointType.fromIndex(i);
                if(handData.getJoint(type).isTracking()){
                    newJoints.add(joints[i]);
                    activeJointsType.add(type);
                }
            }

            if(!newJoints.equals(activeJointsPose)){
                activeJointsPose = newJoints;
                return true;
            }
            return false;
        }

        private void updateTracking(Vector3fc origin,
                                    float worldScale,
                                    float rotationY){
            for (int i = 0; i < joints.length; i++) {
                var jointData = handData.getJoint(VRHandJointType.fromIndex(i));
                if(jointData.isTracking()){
                    joints[i].update(
                            jointData.getPosition(),
                            jointData.getRotation(),
                            jointData.getVector(),
                            origin,
                            rotationY,
                            worldScale
                    );
                }
            }
        }

        private void copyFrom(LocalHandPose other){
            for (int i = 0; i < joints.length; i++) {
                joints[i].copyFrom(other.joints[i]);
            }
        }

        @Override
        public boolean isActive() {
            return handData.isTracking();
        }

        @Override
        public @NotNull List<VRPose> getActiveJointsPose() {
            return activeJointsPose;
        }

        @Override
        public @NotNull List<VRHandJointType> getActiveJointsType() {
            return activeJointsType;
        }

        @Override
        public @Nullable VRPose getJoint(@NotNull VRHandJointType joint) {
            return activeJointsType.contains(joint) ? joints[joint.ordinal()] : null;
        }
    }
}

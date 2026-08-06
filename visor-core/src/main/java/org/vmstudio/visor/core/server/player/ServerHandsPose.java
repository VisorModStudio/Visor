package org.vmstudio.visor.core.server.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.buffer.PoseHandBuffer;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseHand;
import org.vmstudio.visor.api.common.player.VRPoseHands;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.ArrayList;
import java.util.List;

public class ServerHandsPose implements VRPoseHands {

    private final PlayerPoseServerImpl owner;

    private final ServerHandPose leftHand = new ServerHandPose();
    private final ServerHandPose rightHand = new ServerHandPose();

    public ServerHandsPose(@NotNull PlayerPoseServerImpl owner){
        this.owner = owner;
    }

    public void update(PoseDataBuffer poseData,
                       Vector3fc origin){
        var handsBuffer = poseData.hands();

        boolean changed = leftHand.updateActiveJoints(handsBuffer.leftHand());
        changed |= rightHand.updateActiveJoints(handsBuffer.rightHand());
        if(changed){
            owner.resetPoseElements();
        }

        leftHand.update(handsBuffer.leftHand(), origin);
        rightHand.update(handsBuffer.rightHand(), origin);
    }

    public void copyFrom(ServerHandsPose other){
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
        return leftHand.isActive() || rightHand.isActive();
    }

    @Override
    public @NotNull ServerHandPose getLeftHand() {
        return leftHand;
    }

    @Override
    public @NotNull ServerHandPose getRightHand() {
        return rightHand;
    }


    public static class ServerHandPose implements VRPoseHand {

        private final VRPoseImpl[] joints = new VRPoseImpl[VRHandJointType.COUNT];

        private List<VRPose> activeJointsPose = new ArrayList<>();
        private final List<VRHandJointType> activeJointsType = new ArrayList<>();

        private ServerHandPose(){
            for (int i = 0; i < joints.length; i++) {
                joints[i] = new VRPoseImpl();
            }
        }

        private boolean updateActiveJoints(PoseHandBuffer handBuffer){
            List<VRPose> newJoints = new ArrayList<>();

            activeJointsType.clear();

            for(var joint : handBuffer.joints()){
                newJoints.add(joints[joint.type().ordinal()]);
                activeJointsType.add(joint.type());
            }

            if(!newJoints.equals(activeJointsPose)){
                activeJointsPose = newJoints;
                return true;
            }
            return false;
        }

        private void update(PoseHandBuffer handBuffer,
                            Vector3fc origin){
            for(var joint : handBuffer.joints()){
                Vector3f dir = joint.orientation()
                        .transform(VRMathUtils.BACK_VECTOR, new Vector3f());
                joints[joint.type().ordinal()].update(
                        joint.position(),
                        joint.orientation().get(new Matrix4f()),
                        dir,
                        origin,
                        0,
                        1.0f
                );
            }
        }

        private void copyFrom(ServerHandPose other){
            for (int i = 0; i < joints.length; i++) {
                joints[i].copyFrom(other.joints[i]);
            }
        }

        @Override
        public boolean isActive() {
            return !activeJointsPose.isEmpty();
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

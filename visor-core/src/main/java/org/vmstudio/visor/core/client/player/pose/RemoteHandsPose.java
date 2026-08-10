package org.vmstudio.visor.core.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.network.buffer.PoseHandBuffer;
import org.vmstudio.visor.api.common.network.buffer.PoseHandsBuffer;
import org.vmstudio.visor.api.common.player.VRHandJointType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseHand;
import org.vmstudio.visor.api.common.player.VRPoseHands;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.ArrayList;
import java.util.List;

public class RemoteHandsPose implements VRPoseHands {

    private final RemotePlayerPose owner;

    private final RemoteHandPose leftHand = new RemoteHandPose();
    private final RemoteHandPose rightHand = new RemoteHandPose();

    public RemoteHandsPose(@NotNull RemotePlayerPose owner){
        this.owner = owner;
    }

    public void update(PoseHandsBuffer handsBuffer,
                       float turnRotationY){
        boolean changed = leftHand.updateActiveJoints(handsBuffer.leftHand());
        changed |= rightHand.updateActiveJoints(handsBuffer.rightHand());
        if(changed){
            owner.resetPoseElements();
        }

        leftHand.update(
                handsBuffer.leftHand(), turnRotationY,
                owner.getOrigin(), owner.getRotationY(), owner.getWorldScale()
        );
        rightHand.update(
                handsBuffer.rightHand(), turnRotationY,
                owner.getOrigin(), owner.getRotationY(), owner.getWorldScale()
        );
    }

    public void copyFrom(RemoteHandsPose other){
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
    public @NotNull RemoteHandPose getLeftHand() {
        return leftHand;
    }

    @Override
    public @NotNull RemoteHandPose getRightHand() {
        return rightHand;
    }


    public static class RemoteHandPose implements VRPoseHand {

        private final VRPoseImpl[] joints = new VRPoseImpl[VRHandJointType.COUNT];

        private List<VRPose> activeJointsPose = new ArrayList<>();
        private final List<VRHandJointType> activeJointsType = new ArrayList<>();

        private RemoteHandPose(){
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
                            float turnRotationY,
                            Vector3fc origin,
                            float rotationY,
                            float worldScale){
            for(var joint : handBuffer.joints()){
                Vector3f pos = joint.position()
                        .rotateY(-turnRotationY, new Vector3f());
                Matrix4f rotation = new Matrix4f().rotationY(-turnRotationY)
                        .mul(joint.orientation().get(new Matrix4f()));
                Vector3f dir = joint.orientation()
                        .transform(VRMathUtils.FORWARD_VECTOR, new Vector3f())
                        .rotateY(-turnRotationY);

                joints[joint.type().ordinal()].update(
                        pos,
                        rotation,
                        dir,
                        origin,
                        rotationY,
                        worldScale
                );
            }
        }

        private void copyFrom(RemoteHandPose other){
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

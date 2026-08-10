package org.vmstudio.visor.api.common.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VRPoseHand {

    VRPoseHand EMPTY = new VRPoseHand() {
        @Override public boolean isActive() {return false;}
        @Override public @NotNull List<VRPose> getActiveJointsPose() {return List.of();}
        @Override public @NotNull List<VRHandJointType> getActiveJointsType() {return List.of();}
        @Override public @Nullable VRPose getJoint(@NotNull VRHandJointType joint) {return null;}
    };

    boolean isActive();

    @NotNull List<VRPose> getActiveJointsPose();

    @NotNull List<VRHandJointType> getActiveJointsType();

    @Nullable VRPose getJoint(@NotNull VRHandJointType joint);

}

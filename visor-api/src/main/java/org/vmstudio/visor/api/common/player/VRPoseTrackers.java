package org.vmstudio.visor.api.common.player;


import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface VRPoseTrackers {

    boolean isActive();

    List<VRPose> getActiveTrackersPose();

    List<VRBodyPartType> getActiveTrackersType();

    @Nullable VRPose getWaist();
    @Nullable VRPose getChest();

    @Nullable VRPose getLeftFoot();
    @Nullable VRPose getRightFoot();
    @Nullable VRPose getLeftAnkle();
    @Nullable VRPose getRightAnkle();
    @Nullable VRPose getLeftKnee();
    @Nullable VRPose getRightKnee();

    @Nullable VRPose getLeftWrist();
    @Nullable VRPose getRightWrist();
    @Nullable VRPose getLeftElbow();
    @Nullable VRPose getRightElbow();
    @Nullable VRPose getLeftShoulder();
    @Nullable VRPose getRightShoulder();


}

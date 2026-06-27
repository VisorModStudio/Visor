package org.vmstudio.visor.core.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseTrackers;

import java.util.List;

public class RemoteTrackersPose implements VRPoseTrackers {
    private final RemotePlayerPose owner;

    public RemoteTrackersPose(@NotNull RemotePlayerPose owner){
        this.owner = owner;
    }
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public List<VRPose> getActiveTrackersPose() {
        return List.of();
    }

    @Override
    public List<VRBodyPartType> getActiveTrackersType() {
        return List.of();
    }

    @Override
    public @Nullable VRPose getWaist() {
        return null;
    }

    @Override
    public @Nullable VRPose getChest() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftFoot() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightFoot() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftAnkle() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightAnkle() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftKnee() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightKnee() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftWrist() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightWrist() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftElbow() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightElbow() {
        return null;
    }

    @Override
    public @Nullable VRPose getLeftShoulder() {
        return null;
    }

    @Override
    public @Nullable VRPose getRightShoulder() {
        return null;
    }
}

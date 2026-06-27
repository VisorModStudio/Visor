package org.vmstudio.visor.api.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.player.VRBodyPartType;

public interface RawTrackers {

    boolean isTracking();

    @NotNull RawTracker getWaist();
    @NotNull RawTracker getChest();

    @NotNull RawTracker getLeftFoot();
    @NotNull RawTracker getRightFoot();
    @NotNull RawTracker getLeftAnkle();
    @NotNull RawTracker getRightAnkle();
    @NotNull RawTracker getLeftKnee();
    @NotNull RawTracker getRightKnee();

    @NotNull RawTracker getLeftWrist();
    @NotNull RawTracker getRightWrist();
    @NotNull RawTracker getLeftElbow();
    @NotNull RawTracker getRightElbow();
    @NotNull RawTracker getLeftShoulder();
    @NotNull RawTracker getRightShoulder();


    default RawTracker getTracker(@NotNull VRBodyPartType bodyPartType){
        return switch (bodyPartType){
            case WAIST -> getWaist();
            case CHEST -> getChest();
            case LEFT_FOOT -> getLeftFoot();
            case RIGHT_FOOT -> getRightFoot();
            case LEFT_ANKLE -> getLeftAnkle();
            case RIGHT_ANKLE -> getRightAnkle();
            case LEFT_KNEE -> getLeftKnee();
            case RIGHT_KNEE -> getRightKnee();
            case LEFT_WRIST -> getLeftWrist();
            case RIGHT_WRIST -> getRightWrist();
            case LEFT_ELBOW -> getLeftElbow();
            case RIGHT_ELBOW -> getRightElbow();
            case LEFT_SHOULDER -> getLeftShoulder();
            case RIGHT_SHOULDER -> getRightShoulder();
            case HEAD, MAIN_HAND, OFFHAND -> throw new RuntimeException("Tried to get tracker from non-tracker bodyPartType");
        };
    }
}

package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.pose.RawTrackers;
import org.vmstudio.visor.api.common.player.VRBodyPartType;

public class RawTrackersImpl implements RawTrackers {

    @Getter @Setter
    private boolean tracking;

    @Getter
    private final RawTrackerImpl waist = new RawTrackerImpl(VRBodyPartType.WAIST);
    @Getter
    private final RawTrackerImpl chest = new RawTrackerImpl(VRBodyPartType.CHEST);


    @Getter
    private final RawTrackerImpl leftFoot = new RawTrackerImpl(VRBodyPartType.LEFT_FOOT);
    @Getter
    private final RawTrackerImpl rightFoot = new RawTrackerImpl(VRBodyPartType.RIGHT_FOOT);

    @Getter
    private final RawTrackerImpl leftAnkle = new RawTrackerImpl(VRBodyPartType.LEFT_ANKLE);
    @Getter
    private final RawTrackerImpl rightAnkle = new RawTrackerImpl(VRBodyPartType.RIGHT_ANKLE);

    @Getter
    private final RawTrackerImpl leftKnee = new RawTrackerImpl(VRBodyPartType.LEFT_KNEE);
    @Getter
    private final RawTrackerImpl rightKnee = new RawTrackerImpl(VRBodyPartType.RIGHT_KNEE);


    @Getter
    private final RawTrackerImpl leftWrist = new RawTrackerImpl(VRBodyPartType.LEFT_WRIST);
    @Getter
    private final RawTrackerImpl rightWrist = new RawTrackerImpl(VRBodyPartType.RIGHT_WRIST);

    @Getter
    private final RawTrackerImpl leftElbow = new RawTrackerImpl(VRBodyPartType.LEFT_ELBOW);
    @Getter
    private final RawTrackerImpl rightElbow = new RawTrackerImpl(VRBodyPartType.RIGHT_ELBOW);

    @Getter
    private final RawTrackerImpl leftShoulder = new RawTrackerImpl(VRBodyPartType.LEFT_SHOULDER);
    @Getter
    private final RawTrackerImpl rightShoulder = new RawTrackerImpl(VRBodyPartType.RIGHT_SHOULDER);


    @Override
    public @NotNull RawTrackerImpl getTracker(@NotNull VRBodyPartType bodyPartType) {
        return (RawTrackerImpl) RawTrackers.super.getTracker(bodyPartType);
    }
}

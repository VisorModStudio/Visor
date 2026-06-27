package org.vmstudio.visor.core.client.player.pose;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseTrackers;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.raw.RawTrackerImpl;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.*;

public class LocalTrackersPose implements VRPoseTrackers {

    private final LocalPlayerPose owner;

    protected final VRPoseImpl waist;
    protected final VRPoseImpl chest;

    protected final VRPoseImpl leftFoot;
    protected final VRPoseImpl rightFoot;
    protected final VRPoseImpl leftAnkle;
    protected final VRPoseImpl rightAnkle;
    protected final VRPoseImpl leftKnee;
    protected final VRPoseImpl rightKnee;

    protected final VRPoseImpl leftWrist;
    protected final VRPoseImpl rightWrist;
    protected final VRPoseImpl leftElbow;
    protected final VRPoseImpl rightElbow;
    protected final VRPoseImpl leftShoulder;
    protected final VRPoseImpl rightShoulder;

    private final Map<VRPose, RawTrackerImpl> trackersMap;

    @Getter
    private List<VRPose> activeTrackersPose;
    @Getter
    private List<VRBodyPartType> activeTrackersType;


    public LocalTrackersPose(@NotNull LocalPlayerPose owner){
        this.owner = owner;
        this.trackersMap = new LinkedHashMap<>();


        var trackersData = ClientContext.rawPoseHandler.getTrackersData();

        this.waist = new VRPoseImpl();
        this.chest = new VRPoseImpl();
        trackersMap.put(waist, trackersData.getWaist());
        trackersMap.put(chest, trackersData.getChest());

        this.leftFoot = new VRPoseImpl();
        this.rightFoot = new VRPoseImpl();
        this.leftAnkle = new VRPoseImpl();
        this.rightAnkle = new VRPoseImpl();
        this.leftKnee = new VRPoseImpl();
        this.rightKnee = new VRPoseImpl();
        trackersMap.put(leftFoot, trackersData.getLeftFoot());
        trackersMap.put(rightFoot, trackersData.getRightFoot());
        trackersMap.put(leftAnkle, trackersData.getLeftAnkle());
        trackersMap.put(rightAnkle, trackersData.getRightAnkle());
        trackersMap.put(leftKnee, trackersData.getLeftKnee());
        trackersMap.put(rightKnee, trackersData.getRightKnee());

        this.leftWrist = new VRPoseImpl();
        this.rightWrist = new VRPoseImpl();
        this.leftElbow = new VRPoseImpl();
        this.rightElbow = new VRPoseImpl();
        this.leftShoulder = new VRPoseImpl();
        this.rightShoulder = new VRPoseImpl();
        trackersMap.put(leftWrist, trackersData.getLeftWrist());
        trackersMap.put(rightWrist, trackersData.getRightWrist());
        trackersMap.put(leftElbow, trackersData.getLeftElbow());
        trackersMap.put(rightElbow, trackersData.getRightElbow());
        trackersMap.put(leftShoulder, trackersData.getLeftShoulder());
        trackersMap.put(rightShoulder, trackersData.getRightShoulder());

        this.activeTrackersPose = new ArrayList<>();
        this.activeTrackersType = new ArrayList<>();
        updateActiveTrackers(false);

    }

    public void updateTracking(Vector3fc origin,
                               float worldScale,
                               float rotationY){
        updateActiveTrackers(true);

        for(var entry : trackersMap.entrySet()){
            if(entry.getValue().isTracking()){
                entry.getKey().update(
                        entry.getValue().getPosition(),
                        entry.getValue().getRotation(),
                        entry.getValue().getVector(),
                        origin,
                        rotationY,
                        worldScale
                );
            }
        }
    }


    public void copyFrom(LocalTrackersPose other){
        waist.copyFrom(other.waist);
        chest.copyFrom(other.chest);

        leftFoot.copyFrom(other.leftFoot);
        rightFoot.copyFrom(other.rightFoot);
        leftAnkle.copyFrom(other.leftAnkle);
        rightAnkle.copyFrom(other.rightAnkle);
        leftKnee.copyFrom(other.leftKnee);
        rightKnee.copyFrom(other.rightKnee);

        leftWrist.copyFrom(other.leftWrist);
        rightWrist.copyFrom(other.rightWrist);
        leftElbow.copyFrom(other.leftElbow);
        rightElbow.copyFrom(other.rightElbow);
        leftShoulder.copyFrom(other.leftShoulder);
        rightShoulder.copyFrom(other.rightShoulder);
    }

    private void updateActiveTrackers(boolean callReset){
        List<VRPose> newTrackers = new ArrayList<>();

        activeTrackersType.clear();

        if(!isActive()){
            if(!activeTrackersPose.isEmpty()){
                activeTrackersPose.clear();
                if(callReset) {
                    owner.resetPoseElements();
                }
            }
            return;
        }

        for(var entry : trackersMap.entrySet()){
            if(entry.getValue().isTracking()){
                newTrackers.add(entry.getKey());
                activeTrackersType.add(entry.getValue().getType());
            }
        }

        if(!newTrackers.equals(activeTrackersPose)){
            activeTrackersPose = newTrackers;
            if(callReset) {
                owner.resetPoseElements();
            }
        }

    }


    @Override
    public boolean isActive() {
        return ClientContext.rawPoseHandler.getTrackersData().isTracking();
    }


    @Override
    public @Nullable VRPose getWaist() {
        return activeTrackersType.contains(VRBodyPartType.WAIST) ? waist : null;
    }
    @Override
    public @Nullable VRPose getChest() {
        return activeTrackersType.contains(VRBodyPartType.CHEST) ? chest : null;
    }


    @Override
    public @Nullable VRPose getLeftFoot() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_FOOT) ? leftFoot : null;
    }
    @Override
    public @Nullable VRPose getRightFoot() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_FOOT) ? rightFoot : null;
    }
    @Override
    public @Nullable VRPose getLeftAnkle() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_ANKLE) ? leftAnkle : null;
    }
    @Override
    public @Nullable VRPose getRightAnkle() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_ANKLE) ? rightAnkle : null;
    }
    @Override
    public @Nullable VRPose getLeftKnee() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_KNEE) ? leftKnee : null;
    }
    @Override
    public @Nullable VRPose getRightKnee() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_KNEE) ? rightKnee : null;
    }


    @Override
    public @Nullable VRPose getLeftWrist() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_WRIST) ? leftWrist : null;
    }
    @Override
    public @Nullable VRPose getRightWrist() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_WRIST) ? rightWrist : null;
    }
    @Override
    public @Nullable VRPose getLeftElbow() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_ELBOW) ? leftElbow : null;
    }
    @Override
    public @Nullable VRPose getRightElbow() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_ELBOW) ? rightElbow : null;
    }
    @Override
    public @Nullable VRPose getLeftShoulder() {
        return activeTrackersType.contains(VRBodyPartType.LEFT_SHOULDER) ? leftShoulder : null;
    }
    @Override
    public @Nullable VRPose getRightShoulder() {
        return activeTrackersType.contains(VRBodyPartType.RIGHT_SHOULDER) ? rightShoulder : null;
    }


}

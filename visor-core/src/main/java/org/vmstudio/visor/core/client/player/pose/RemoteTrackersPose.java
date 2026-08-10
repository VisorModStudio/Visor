package org.vmstudio.visor.core.client.player.pose;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.common.network.buffer.PoseElementBuffer;
import org.vmstudio.visor.api.common.network.buffer.PoseTrackersBuffer;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseTrackers;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RemoteTrackersPose implements VRPoseTrackers {

    private final RemotePlayerPose owner;

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

    private final Map<VRPose, Function<PoseTrackersBuffer, PoseElementBuffer>> trackersMap;

    @Getter
    private List<VRPose> activeTrackersPose;
    @Getter
    private List<VRBodyPartType> activeTrackersType;


    public RemoteTrackersPose(@NotNull RemotePlayerPose owner){
        this.owner = owner;
        this.trackersMap = new LinkedHashMap<>();

        this.waist = new VRPoseImpl();
        this.chest = new VRPoseImpl();
        trackersMap.put(waist, PoseTrackersBuffer::waist);
        trackersMap.put(chest, PoseTrackersBuffer::chest);

        this.leftFoot = new VRPoseImpl();
        this.rightFoot = new VRPoseImpl();
        this.leftAnkle = new VRPoseImpl();
        this.rightAnkle = new VRPoseImpl();
        this.leftKnee = new VRPoseImpl();
        this.rightKnee = new VRPoseImpl();
        trackersMap.put(leftFoot, PoseTrackersBuffer::leftFoot);
        trackersMap.put(rightFoot, PoseTrackersBuffer::rightFoot);
        trackersMap.put(leftAnkle, PoseTrackersBuffer::leftAnkle);
        trackersMap.put(rightAnkle, PoseTrackersBuffer::rightAnkle);
        trackersMap.put(leftKnee, PoseTrackersBuffer::leftKnee);
        trackersMap.put(rightKnee, PoseTrackersBuffer::rightKnee);

        this.leftWrist = new VRPoseImpl();
        this.rightWrist = new VRPoseImpl();
        this.leftElbow = new VRPoseImpl();
        this.rightElbow = new VRPoseImpl();
        this.leftShoulder = new VRPoseImpl();
        this.rightShoulder = new VRPoseImpl();
        trackersMap.put(leftWrist, PoseTrackersBuffer::leftWrist);
        trackersMap.put(rightWrist, PoseTrackersBuffer::rightWrist);
        trackersMap.put(leftElbow, PoseTrackersBuffer::leftElbow);
        trackersMap.put(rightElbow, PoseTrackersBuffer::rightElbow);
        trackersMap.put(leftShoulder, PoseTrackersBuffer::leftShoulder);
        trackersMap.put(rightShoulder, PoseTrackersBuffer::rightShoulder);

        this.activeTrackersPose = new ArrayList<>();
        this.activeTrackersType = new ArrayList<>();

    }

    public void update(PoseTrackersBuffer trackersBuffer,
                       float turnRotationY){
        updateActiveTrackers(trackersBuffer);

        for(var entry : trackersMap.entrySet()){
            var poseElement = entry.getValue().apply(trackersBuffer);
            if(poseElement == null){
                continue;
            }
            Vector3f pos = poseElement.position()
                    .rotateY(-turnRotationY, new Vector3f());
            Matrix4f rotation = new Matrix4f().rotationY(-turnRotationY)
                    .mul(poseElement.orientation().get(new Matrix4f()));
            Vector3f dir = poseElement.orientation()
                    .transform(VRMathUtils.FORWARD_VECTOR, new Vector3f())
                    .rotateY(-turnRotationY);

            entry.getKey().update(
                    pos,
                    rotation,
                    dir,
                    owner.getOrigin(),
                    owner.getRotationY(),
                    owner.getWorldScale()
            );
        }
    }

    public void copyFrom(RemoteTrackersPose other){
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

    private void updateActiveTrackers(PoseTrackersBuffer trackersBuffer){
        List<VRPose> newTrackers = new ArrayList<>();

        activeTrackersType.clear();

        for(var entry : trackersMap.entrySet()){
            var poseElement = entry.getValue().apply(trackersBuffer);
            if(poseElement != null){
                newTrackers.add(entry.getKey());
                activeTrackersType.add(poseElement.type());
            }
        }

        if(!newTrackers.equals(activeTrackersPose)){
            activeTrackersPose = newTrackers;
            owner.resetPoseElements();
        }
    }


    @Override
    public boolean isActive() {
        return !activeTrackersPose.isEmpty();
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

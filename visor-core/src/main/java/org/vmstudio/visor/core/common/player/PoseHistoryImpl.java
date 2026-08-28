package org.vmstudio.visor.core.common.player;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPoseHistory;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public class PoseHistoryImpl implements VRPoseHistory {
    private final List<VRPlayerPose> history = new ArrayList<>();
    private final VRPlayerPose relevantPose;

    public PoseHistoryImpl(VRPlayerPose relevantPose){
        this.relevantPose = relevantPose;
        history.add(relevantPose);
    }

    @Override
    public Vector3f netMovement(VRBodyPartType bodyPart, int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.size() <= 1) {
            return new Vector3f();
        }

        maxTicksBack = clampTicksBack(maxTicksBack);
        var last = safePosition(history.get(0), bodyPart);
        var old = safePosition(history.get(maxTicksBack), bodyPart);
        if (last == null || old == null) {
            return new Vector3f();
        }

        return last.sub(old, new Vector3f());
    }

    @Override
    public Vector3f headPivotNetMovement(int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.size() <= 1) {
            return new Vector3f();
        }

        maxTicksBack = clampTicksBack(maxTicksBack);
        var last = history.get(0).getHeadPivot();
        var old = history.get(maxTicksBack).getHeadPivot();

        return last.sub(old, new Vector3f());
    }

    @Override
    public double averageSpeed(VRBodyPartType bodyPart, int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.size() <= 1) {
            return 0;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);

        var deltaValues = DoubleStream.builder();
        for (int i = 0; i < maxTicksBack; i++) {
            var current = safePosition(history.get(i), bodyPart);
            var previous = safePosition(history.get(i + 1), bodyPart);
            if (current == null || previous == null) {
                continue;
            }
            deltaValues.add(current.distance(previous));
        }

        return deltaValues.build()
                .average()
                .orElse(0);
    }

    @Override
    public double headPivotAverageSpeed(int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.size() <= 1) {
            return 0;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);

        var deltaValues = DoubleStream.builder();
        for (int i = 0; i < maxTicksBack; i++) {
            var current = history.get(i).getHeadPivot();
            var previous = history.get(i + 1).getHeadPivot();
            deltaValues.add(current.distance(previous));
        }

        return deltaValues.build()
                .average()
                .orElse(0);
    }

    @Override
    public Vector3f averagePosition(VRBodyPartType bodyPart, int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.isEmpty()) {
            return null;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);

        var xCoordinates = DoubleStream.builder();
        var yCoordinates = DoubleStream.builder();
        var zCoordinates = DoubleStream.builder();
        boolean empty = true;
        for (int i = 0; i < maxTicksBack; i++) {
            var pos = safePosition(history.get(i), bodyPart);
            if (pos != null) {
                xCoordinates.add(pos.x());
                yCoordinates.add(pos.y());
                zCoordinates.add(pos.z());
                empty = false;
            }
        }
        if (empty) {
            return null;
        }
        return new Vector3f(
                (float) xCoordinates.build().average().orElse(0),
                (float) yCoordinates.build().average().orElse(0),
                (float) zCoordinates.build().average().orElse(0)
        );
    }

    @Override
    public Vector3f headPivotAveragePosition(int maxTicksBack) {
        requireValidTicks(maxTicksBack);
        if (history.isEmpty()) {
            return null;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        if (maxTicksBack == 0) {
            return null;
        }

        var xCoordinates = DoubleStream.builder();
        var yCoordinates = DoubleStream.builder();
        var zCoordinates = DoubleStream.builder();
        for (int i = 0; i < maxTicksBack; i++) {
            var pos = history.get(i).getHeadPivot();
            xCoordinates.add(pos.x());
            yCoordinates.add(pos.y());
            zCoordinates.add(pos.z());
        }
        return new Vector3f(
                (float) xCoordinates.build().average().orElse(0),
                (float) yCoordinates.build().average().orElse(0),
                (float) zCoordinates.build().average().orElse(0)
        );
    }

    public void addEntry(VRPlayerPose entry){
        history.set(0, entry);
        history.add(0, relevantPose);
        if (history.size() > HISTORY_LIMIT) {
            history.remove(history.size() - 1);
        }
    }

    @Override
    public @Nullable VRPlayerPose getEntry(int ticksBack) {
        if(ticksBack >= history.size()){
            return null;
        }
        return history.get(ticksBack);
    }

    @Override
    public List<VRPlayerPose> getAllHistory() {
        return List.copyOf(history);
    }

    @Override
    public int getHistorySize() {
        return history.size();
    }

    private void requireValidTicks(int ticksBack) {
        if (ticksBack < 0 || ticksBack > HISTORY_LIMIT) {
            throw new IllegalArgumentException("ticksBack must be within 0.." + HISTORY_LIMIT + ", got " + ticksBack);
        }
    }

    private int clampTicksBack(int maxTicksBack) {
        return Mth.clamp(maxTicksBack, 0, history.size()-1);
    }

    private Vector3fc safePosition(VRPlayerPose pose, VRBodyPartType bodyPart) {
        var bodyPartPose = pose.getPose(bodyPart);
        return bodyPartPose == null ? null : bodyPartPose.getPosition();
    }

    public void clear() {
        history.clear();
        history.add(relevantPose);
    }
}

package org.vmstudio.visor.core.common.player;

import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPoseHistory;
import org.vmstudio.visor.api.common.player.VRTrackableBodyPart;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PoseHistoryImpl implements VRPoseHistory {

    private final LinkedList<VRPlayerPose> history = new LinkedList<>();

    private final VRPlayerPose relevantPose;
    public PoseHistoryImpl(VRPlayerPose relevantPose){
        this.relevantPose = relevantPose;
        history.addFirst(relevantPose);
    }


    @Override
    public Vector3f netMovement(VRTrackableBodyPart bodyPart, int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return (Vector3f) VRMathUtils.ZERO_VECTOR;
        }

        maxTicksBack = clampTicksBack(maxTicksBack);

        var last = history.getFirst().getPose(bodyPart).getPosition();

        var old = history.get(maxTicksBack).getPose(bodyPart).getPosition();

        return last.sub(old, new Vector3f());
    }

    @Override
    public Vector3f headPivotNetMovement(int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return (Vector3f) VRMathUtils.ZERO_VECTOR;
        }

        maxTicksBack = clampTicksBack(maxTicksBack);

        var last = history.getFirst().getHeadPivot();

        var old = history.get(maxTicksBack).getHeadPivot();

        return last.sub(old, new Vector3f());
    }

    @Override
    public double averageSpeed(VRTrackableBodyPart bodyPart, int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return 0;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        List<Float> deltas = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            var newer = history.get(i).getPose(bodyPart).getPosition();
            var older = history.get(i + 1).getPose(bodyPart).getPosition();

            deltas.add(newer.distance(older));
        }
        return deltas.stream()
                .mapToDouble(Double::valueOf)
                .average()
                .orElse(0);
    }

    @Override
    public double headPivotAverageSpeed(int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return 0;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        List<Float> deltas = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            var newer = history.get(i).getHeadPivot();
            var older = history.get(i + 1).getHeadPivot();

            deltas.add(newer.distance(older));
        }
        return deltas.stream()
                .mapToDouble(Double::valueOf)
                .average()
                .orElse(0);
    }

    @Override
    public Vector3f averagePosition(VRTrackableBodyPart bodyPart, int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.isEmpty()) {
            return null;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        List<Vector3fc> positions = new ArrayList<>(maxTicksBack);
        int i = 0;
        for (var pose : this.history) {
            var pos = pose.getPose(bodyPart).getPosition();
            positions.add(pos);
            if (++i >= maxTicksBack) break;
        }
        if (positions.isEmpty()) {
            return null;
        }
        return new Vector3f(
                (float) positions.stream().mapToDouble(Vector3fc::x).average().orElse(0),
                (float) positions.stream().mapToDouble(Vector3fc::y).average().orElse(0),
                (float) positions.stream().mapToDouble(Vector3fc::z).average().orElse(0)
        );
    }

    @Override
    public Vector3f headPivotAveragePosition(int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.isEmpty()) {
            return null;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        List<Vector3fc> positions = new ArrayList<>(maxTicksBack);
        int i = 0;
        for (var pose : this.history) {
            var pos = pose.getHeadPivot();
            positions.add(pos);
            if (i++ >= maxTicksBack) break;
        }
        if (positions.isEmpty()) {
            return null;
        }
        return new Vector3f(
                (float) positions.stream().mapToDouble(Vector3fc::x).average().orElse(0),
                (float) positions.stream().mapToDouble(Vector3fc::y).average().orElse(0),
                (float) positions.stream().mapToDouble(Vector3fc::z).average().orElse(0)
        );
    }

    public void addEntry(VRPlayerPose entry){
        history.removeFirst();
        history.addFirst(entry);
        history.addFirst(relevantPose);
        if (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }
    }


    @Override
    public VRPlayerPose getEntry(int ticksBack) {
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




    private void checkTicksBack(int ticksBack) {
        if (ticksBack < 0 || ticksBack > HISTORY_LIMIT) {
            throw new IllegalArgumentException("Value must be between 0 and " + HISTORY_LIMIT);
        }
    }

    private int clampTicksBack(int maxTicksBack) {
        return Mth.clamp(maxTicksBack, 0, history.size()-1);
    }


    public void clear(){
        history.clear();
        history.addFirst(relevantPose);
    }
}

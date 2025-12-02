package me.phoenixra.visor.core.common.player;

import me.phoenixra.visor.api.common.player.PlayerPose;
import me.phoenixra.visor.api.common.player.PoseHistory;
import me.phoenixra.visor.api.common.player.VRBodyPart;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class PoseHistoryImpl implements PoseHistory {

    private final LinkedList<PlayerPose> history = new LinkedList<>();

    private final PlayerPose currentPose;
    public PoseHistoryImpl(PlayerPose currentPose){
        this.currentPose = currentPose;
        history.addFirst(currentPose);
    }
    @Override
    public Vector3f netMovement(VRBodyPart bodyPart, int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return (Vector3f) VRMathUtils.ZERO_VECTOR;
        }

        maxTicksBack = clampTicksBack(maxTicksBack);

        var last = history.getFirst().getPoseElement(bodyPart).getPosition();

        var old = history.get(maxTicksBack).getPoseElement(bodyPart).getPosition();

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
    public double averageSpeed(VRBodyPart bodyPart, int maxTicksBack) {
        checkTicksBack(maxTicksBack);
        if (history.size() <= 1) {
            return 0;
        }
        maxTicksBack = clampTicksBack(maxTicksBack);
        List<Float> deltas = new ArrayList<>(maxTicksBack);
        for (int i = 0; i < maxTicksBack; i++) {
            var newer = history.get(i).getPoseElement(bodyPart).getPosition();
            var older = history.get(i + 1).getPoseElement(bodyPart).getPosition();

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

    public void addEntry(PlayerPose entry){
        history.removeFirst();
        history.addFirst(entry);
        history.addFirst(currentPose);
        if (history.size() > HISTORY_LIMIT) {
            history.removeLast();
        }
    }


    @Override
    public PlayerPose getEntry(int ticksBack) {
        return history.get(ticksBack);
    }

    @Override
    public List<PlayerPose> getAllHistory() {
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


    public void dispose(){
        history.clear();
    }
}

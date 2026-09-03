package org.vmstudio.visor.core.client.player.height;

import org.vmstudio.visor.api.client.player.pose.RawHmd;
import org.vmstudio.visor.api.client.settings.VRClientSettings;

import java.util.Arrays;


public final class HeightEstimator {
    private static final float BIN = 0.01f;
    private static final float MIN = VRClientSettings.MIN_HEIGHT;
    private static final float MAX = 2.6f;
    private static final float DECAY = 0.99988f;
    private static final float LEVEL_LIMIT = 0.42f;

    private final float[] bin = new float[(int) ((MAX - MIN) / BIN) + 1];
    private float mass;

    public void sample(RawHmd hmd) {
        var pivots = hmd.getPivotHistory();
        if (pivots.averageSpeed(0.5f) > 0.2f
                || Math.abs(hmd.getVector().y()) > LEVEL_LIMIT) {
            return;
        }
        float y = pivots.averagePosition(0.5f).y;
        if (!(y >= MIN && y <= MAX)) {
            return;
        }
        for (int i = 0; i < bin.length; i++) {
            bin[i] *= DECAY;
        }
        mass = mass * DECAY + 1f;
        bin[(int) ((y - MIN) / BIN)] += 1f;
    }


    public float estimate() {
        if (mass < 60f) {
            return Float.NaN;
        }
        float target = mass * 0.9f;
        float acc = 0f;
        for (int i = 0; i < bin.length; i++) {
            acc += bin[i];
            if (acc >= target) {
                return MIN + i * BIN;
            }
        }
        return Float.NaN;
    }

    public void reset() {
        Arrays.fill(bin, 0f);
        mass = 0f;
    }
}

package org.vmstudio.visor.core.client.player.height;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public final class PlayerHeightTracker {
    private static final float DEAD_ZONE = 0.02f; //height change less than this is ignored
    private static final float STEP_PER_TICK = 0.01f;
    private static final int MEASURE_TICKS = 60;
    private static final long SAVE_INTERVAL = 10_000L;

    private final HeightEstimator estimator = new HeightEstimator();

    private float applied = VRPlayer.DEFAULT_FULL_HEIGHT;
    private boolean started;

    @Getter
    private int measureTicksLeft = -1;

    private boolean saveQueued;
    private long lastSaveMs;
    private boolean hintShown;
    private boolean announceAutoSet;

    public void tick() {
        var hmd = ClientContext.rawPoseHandler.getHmdData();

        if (measureTicksLeft >= 0) {
            if (measureTicksLeft == 0) {
                finishMeasure(hmd.isTracking());
            }
            measureTicksLeft--;
        }

        if (VRClientSettings.isHeightAuto() && hmd.isTracking()) {
            estimator.sample(hmd);
            autoUpdate();
        }

        smooth();
        flushSave();
        notifyPlayer();
    }

    public void startMeasure() {
        measureTicksLeft = MEASURE_TICKS;
    }

    public boolean isMeasuring() {
        return measureTicksLeft >= 0;
    }

    public int getMeasureSecondsLeft() {
        return measureTicksLeft / 20 + 1;
    }


    private void autoUpdate() {
        float estimate = estimator.estimate();
        if (Float.isNaN(estimate)) {
            return;
        }
        if (!VRClientSettings.isFullHeightMeasured()) {
            store(estimate);
            announceAutoSet = true;
            return;
        }
        float stored = VRClientSettings.getFullHeight();
        if (Math.abs(estimate - stored) / stored < DEAD_ZONE) {
            return;
        }

        if (estimate > stored || isSafeMoment()) {
            store(estimate);
        }
    }

    private void smooth() {
        float target = VRClientSettings.getFullHeight();
        boolean snap = !started || VRRenderState.getSceneType().isMainMenu();
        applied = snap
                ? target
                : applied + Mth.clamp(target - applied, -STEP_PER_TICK, STEP_PER_TICK);
        started = true;
        VRClientSettings.setFullHeightApplied(applied);
    }

    private void finishMeasure(boolean tracking) {
        float height = tracking
                ? ClientContext.rawPoseHandler.getHmdData().getPivotHistory().averagePosition(1.0f).y
                : Float.NaN;
        var chat = MC.gui.getChat();
        if (!(height >= VRClientSettings.MIN_HEIGHT)) {
            chat.addMessage(Component.translatable("visor.messages.height_calibration_failed"));
            return;
        }
        store(height);
        estimator.reset();
        lastSaveMs = 0L;
        chat.addMessage(Component.translatable("visor.messages.height_measured", pivotToActualHeight(height)));
    }

    private void store(float height) {
        VRClientSettings.setFullHeight(height);
        saveQueued = true;
    }

    private void flushSave() {
        if (!saveQueued) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastSaveMs < SAVE_INTERVAL) {
            return;
        }
        ClientContext.settingsManager.saveOptions();
        lastSaveMs = now;
        saveQueued = false;
    }

    private void notifyPlayer() {
        if (MC.player == null) {
            return;
        }
        var chat = MC.gui.getChat();
        if (announceAutoSet) {
            announceAutoSet = false;
            chat.addMessage(Component.translatable(
                    "visor.messages.height_auto_set",
                    pivotToActualHeight(VRClientSettings.getFullHeight())
            ));
        } else if (!hintShown
                && VRClientSettings.isHeightAuto()
                && !VRClientSettings.isFullHeightMeasured()) {
            hintShown = true;
            chat.addMessage(Component.translatable("visor.messages.height_auto_hint"));
        }
    }


    private static boolean isSafeMoment() {
        return MC.screen != null || VRRenderState.getSceneType().isMainMenu();
    }



    public static int pivotToActualHeight(float pivotHeight) {
        return Math.round((pivotHeight + 0.21f) * 100f);
    }

    public static float actualHeightToPivot(int statureCm) {
        return statureCm / 100f - 0.21f;
    }
}

package org.vmstudio.visor.core.client.input;

import me.phoenixra.atumvr.api.input.treadmill.AtumVRTreadmillView;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class TreadmillInput {

    //gait speed giving a full input impulse, before the user scale
    private static final float FULL_SPEED_MPS = 2.0f;
    private static final float MIN_IMPULSE = 0.02f;

    private static final Vector3f cachedVelocity = new Vector3f();

    private TreadmillInput() {
    }

    /**
     * Convert the treadmill gait into a movement input vector
     */
    public static @Nullable Vector2f pollMovement(@NotNull Vector2f dest) {
        if (!VRClientSettings.isTreadmillEnabled()) {
            return null;
        }
        AtumVRTreadmillView treadmill = ClientContext.visor.getVrProvider()
                .getInputHandler().getVRTreadmill();
        if (!treadmill.isActive()
                || treadmill.getSpeed() <= 0f
                || treadmill.getMotion().lengthSquared() == 0f) {
            return null;
        }

        float impulse = Math.min(
                treadmill.getSpeed()
                        * VRClientSettings.getTreadmillSpeedScale() / FULL_SPEED_MPS,
                1f
        );
        if (impulse < MIN_IMPULSE) {
            return null;
        }

        var pose = ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK);

        float travelYaw;
        if (treadmill.getVelocity(cachedVelocity) != null) {
            cachedVelocity.rotateY(pose.getRotationY());
            travelYaw = (float) Mth.atan2(-cachedVelocity.x, cachedVelocity.z);
        } else {
            //no direction sensor, gait is relative to the HMD facing
            var motion = treadmill.getMotion();
            travelYaw = pose.getHmd().getYaw()
                    + (float) Mth.atan2(motion.x(), motion.y());
        }

        VRPose rotationElement = ClientContext.localPlayer
                .getRotationElement(PlayerPoseType.TICK);
        if (MC.player != null && MC.player.isSwimming()) {
            rotationElement = pose.getHmd();
        }

        float relativeYaw = travelYaw - rotationElement.getYaw();
        return dest.set(
                impulse * Mth.sin(relativeYaw),
                impulse * Mth.cos(relativeYaw)
        );
    }
}

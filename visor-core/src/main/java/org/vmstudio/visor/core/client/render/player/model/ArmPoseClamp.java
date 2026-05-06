package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.util.Mth;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class ArmPoseClamp {
    private ArmPoseClamp() {}

    // ---- arm clamps (the rigid cube) ----
    public static final float CROSS_BODY_LIMIT = Mth.DEG_TO_RAD * 35.0F;
    public static final float BEHIND_LIMIT     = Mth.DEG_TO_RAD * 170.0F;
    public static final float PITCH_UP_LIMIT   = Mth.DEG_TO_RAD * 90.0F;
    public static final float PITCH_DOWN_LIMIT = Mth.DEG_TO_RAD * 90.0F;


    private static final float POLE_HORIZ_THRESHOLD_SQ = 0.05f * 0.05f;

    private enum Anchor { NONE, LO, HI }

    private static final class PerPlayerState {
        Anchor leftAnchor  = Anchor.NONE;
        Anchor rightAnchor = Anchor.NONE;
        Float lastWorldYawLeft;
        Float lastWorldYawRight;
    }

    private static final ConcurrentHashMap<UUID, PerPlayerState> STATE = new ConcurrentHashMap<>();

    public static final class ArmFrame {
        public final float armPitch;
        public final float armYawDelta;

        ArmFrame(float armPitch, float armYawDelta) {
            this.armPitch = armPitch;
            this.armYawDelta = armYawDelta;
        }
    }

    public static float wrapToPi(float angle) {
        angle %= Mth.TWO_PI;
        if (angle >  Mth.PI) angle -= Mth.TWO_PI;
        if (angle < -Mth.PI) angle += Mth.TWO_PI;
        return angle;
    }

    public static float clampPitch(float pitch) {
        return Mth.clamp(pitch, -PITCH_DOWN_LIMIT, PITCH_UP_LIMIT);
    }

    public static float clampYawDeltaWithHysteresis(UUID playerId,
                                                    float handYaw,
                                                    float bodyYaw,
                                                    boolean leftArm) {
        float delta = wrapToPi(handYaw - bodyYaw);
        float lo = leftArm ? -BEHIND_LIMIT     : -CROSS_BODY_LIMIT;
        float hi = leftArm ?  CROSS_BODY_LIMIT :  BEHIND_LIMIT;

        PerPlayerState state = STATE.computeIfAbsent(playerId, id -> new PerPlayerState());

        if (delta >= lo && delta <= hi) {
            if (leftArm) state.leftAnchor  = Anchor.NONE;
            else         state.rightAnchor = Anchor.NONE;
            return delta;
        }

        Anchor current = leftArm ? state.leftAnchor : state.rightAnchor;
        Anchor side;
        if (current != Anchor.NONE) {
            side = current;
        } else {
            float dLo = Math.abs(wrapToPi(lo - delta));
            float dHi = Math.abs(wrapToPi(hi - delta));
            side = dLo <= dHi ? Anchor.LO : Anchor.HI;
        }

        if (leftArm) state.leftAnchor  = side;
        else         state.rightAnchor = side;

        return side == Anchor.LO ? lo : hi;
    }

    public static float clampYawDelta(float handYaw, float bodyYaw, boolean leftArm) {
        float delta = wrapToPi(handYaw - bodyYaw);
        float lo = leftArm ? -BEHIND_LIMIT     : -CROSS_BODY_LIMIT;
        float hi = leftArm ?  CROSS_BODY_LIMIT :  BEHIND_LIMIT;
        if (delta >= lo && delta <= hi) return delta;
        float dLo = Math.abs(wrapToPi(lo - delta));
        float dHi = Math.abs(wrapToPi(hi - delta));
        return dLo <= dHi ? lo : hi;
    }


    public static ArmFrame solveArmFrame(UUID playerId,
                                         VRPose handPose,
                                         float bodyYaw,
                                         boolean leftArm) {
        Vector3fc aim = handPose.getDirection();
        float ax = aim.x();
        float ay = aim.y();
        float az = aim.z();

        float aimLen = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        float pitch = (aimLen > 1.0e-6f)
                ? (float) Math.asin(Mth.clamp(ay / aimLen, -1.0f, 1.0f))
                : 0.0f;

        PerPlayerState state = STATE.computeIfAbsent(playerId, id -> new PerPlayerState());

        float horizLenSq = ax * ax + az * az;
        float worldYaw;
        if (horizLenSq > POLE_HORIZ_THRESHOLD_SQ) {
            worldYaw = (float) Mth.atan2(-ax, az);
            if (leftArm) state.lastWorldYawLeft  = worldYaw;
            else         state.lastWorldYawRight = worldYaw;
        } else {

            Float last = leftArm ? state.lastWorldYawLeft : state.lastWorldYawRight;
            worldYaw = last != null ? last : bodyYaw;
        }

        float armPitch    = clampPitch(pitch);
        float armYawDelta = clampYawDeltaWithHysteresis(playerId, worldYaw, bodyYaw, leftArm);

        return new ArmFrame(armPitch, armYawDelta);
    }

    public static void forgetPlayer(UUID playerId) {
        STATE.remove(playerId);
    }
}
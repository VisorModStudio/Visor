package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArmPoseClamp {
    private ArmPoseClamp() {}

    public static final float CROSS_BODY_LIMIT = Mth.DEG_TO_RAD * 30.0F;
    public static final float BEHIND_LIMIT     = Mth.DEG_TO_RAD * 170.0F;
    public static final float PITCH_UP_LIMIT   = Mth.DEG_TO_RAD * 85.0F;
    public static final float PITCH_DOWN_LIMIT = Mth.DEG_TO_RAD * 85.0F;

    public static final float WRIST_FLEX_LIMIT   = Mth.DEG_TO_RAD * 50.0F;
    public static final float WRIST_EXTEND_LIMIT = Mth.DEG_TO_RAD * 50.0F;
    public static final float WRIST_TWIST_LIMIT  = Mth.DEG_TO_RAD * 60.0F;
    public static final float WRIST_DEV_RADIAL   = Mth.DEG_TO_RAD * 15.0F;
    public static final float WRIST_DEV_ULNAR    = Mth.DEG_TO_RAD * 25.0F;

    private enum Anchor { NONE, LO, HI }
    private static final class PerPlayerState {
        Anchor leftAnchor  = Anchor.NONE;
        Anchor rightAnchor = Anchor.NONE;
    }
    private static final ConcurrentHashMap<UUID, PerPlayerState> STATE = new ConcurrentHashMap<>();

    public static final class ArmFrame {
        public final float armPitch;
        public final float armYawDelta;
        public final Quaternionf wristResidual;
        ArmFrame(float armPitch, float armYawDelta, Quaternionf wristResidual) {
            this.armPitch = armPitch;
            this.armYawDelta = armYawDelta;
            this.wristResidual = wristResidual;
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
        float rawPitch    = handPose.getPitch();
        float rawHandYaw  = handPose.getYaw();
        float rawRoll     = handPose.getRoll();

        float armPitch    = clampPitch(rawPitch);
        float armYawDelta = clampYawDeltaWithHysteresis(playerId, rawHandYaw, bodyYaw, leftArm);

        float rawYawDelta = wrapToPi(rawHandYaw - bodyYaw);

        float dPitch = wrapToPi(rawPitch    - armPitch);
        float dYaw   = wrapToPi(rawYawDelta - armYawDelta);
        float dRoll  = wrapToPi(rawRoll);

        dPitch = Mth.clamp(dPitch, -WRIST_EXTEND_LIMIT, WRIST_FLEX_LIMIT);
        dYaw   = Mth.clamp(dYaw,   -WRIST_DEV_ULNAR,    WRIST_DEV_RADIAL);
        dRoll  = Mth.clamp(dRoll,  -WRIST_TWIST_LIMIT,  WRIST_TWIST_LIMIT);

        Quaternionf wrist = new Quaternionf().rotationY(dRoll);
        wrist.premul(new Quaternionf().rotationX(dPitch));
        wrist.premul(new Quaternionf().rotationZ(dYaw));

        return new ArmFrame(armPitch, armYawDelta, wrist);
    }

    public static void forgetPlayer(UUID playerId) {
        STATE.remove(playerId);
    }
}
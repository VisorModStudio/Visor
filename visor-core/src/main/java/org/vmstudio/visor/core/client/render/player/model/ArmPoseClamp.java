package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.util.Mth;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class ArmPoseClamp {
    private ArmPoseClamp() {}

    // ---- arm clamps (the rigid cube) ----
    /** Across the chest midline before clipping into the torso. */
    public static final float CROSS_BODY_LIMIT = Mth.DEG_TO_RAD * 35.0F;
    /** Forward-of-body to ~10° short of straight behind. */
    public static final float BEHIND_LIMIT     = Mth.DEG_TO_RAD * 170.0F;
    /** Up from horizontal — full reach to overhead. */
    public static final float PITCH_UP_LIMIT   = Mth.DEG_TO_RAD * 90.0F;
    /** Down from horizontal — full reach toward the feet. */
    public static final float PITCH_DOWN_LIMIT = Mth.DEG_TO_RAD * 90.0F;

    /**
     * If the aim's horizontal projection length squared is below this threshold,
     * the controller is treated as "near the up/down pole" and the arm yaw is
     * frozen to the previously observed stable value. Otherwise tiny
     * horizontal-component changes — or wrist twist while pointing straight
     * up/down — make atan2 swing wildly and the cube spins around its length.
     * 0.05² → kicks in within ≈3° of vertical.
     */
    private static final float POLE_HORIZ_THRESHOLD_SQ = 0.05f * 0.05f;

    private enum Anchor { NONE, LO, HI }

    private static final class PerPlayerState {
        Anchor leftAnchor  = Anchor.NONE;
        Anchor rightAnchor = Anchor.NONE;
        // Last well-defined world yaw of the aim direction, per arm. Used to
        // freeze the arm's yaw when the controller is pointing nearly straight
        // up or down so the cube doesn't spin around its own length.
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

    /**
     * Computes a roll-free arm pose driven solely by the controller's aim
     * direction ({@link VRPose#getDirection()}, the forward vector of the
     * controller in world space).
     * <p>
     * The arm cube is rotated only in pitch + yawDelta with zRot = 0, so any
     * pronation / supination of the wrist is intentionally discarded — that's
     * what gives "no roll on the hands" in third person. The held item rides
     * on this same roll-free frame via vanilla {@code translateToHand}, so it
     * also follows the aim vector and never rolls.
     * <p>
     * Pitch is taken straight from the aim's Y component (asin is well-defined
     * everywhere). Yaw is taken from the aim's horizontal projection; near the
     * up/down pole the projection vanishes and atan2 becomes ill-conditioned,
     * so we freeze the yaw to its previously observed stable value to stop the
     * arm cube from spinning around its own length. (This is the edge case the
     * older {@code VRPose#getYaw()} fallback didn't handle — it tried to
     * derive a yaw from the controller's swing-up vector, which is exactly the
     * wrist twist we're trying to discard.)
     */
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
            // Aim is straight up / down: freeze to the previous stable yaw so
            // the cube doesn't spin around its own length while the user
            // twists the controller. If we've never had a stable value, fall
            // back to the body yaw (arm pointing forward).
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
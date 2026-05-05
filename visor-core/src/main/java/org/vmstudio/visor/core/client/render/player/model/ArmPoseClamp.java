package org.vmstudio.visor.core.client.render.player.model;

import net.minecraft.util.Mth;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
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

    // ---- wrist clamps (drive the held item) ----
    /** Pronation / supination of the wrist around the forearm direction. */
    public static final float WRIST_TWIST_LIMIT = Mth.DEG_TO_RAD * 80.0F;
    /** Combined wrist flex/extension + deviation magnitude. */
    public static final float WRIST_SWING_LIMIT = Mth.DEG_TO_RAD * 55.0F;

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
        float rawPitch   = handPose.getPitch();
        float rawHandYaw = handPose.getYaw();

        float armPitch    = clampPitch(rawPitch);
        float armYawDelta = clampYawDeltaWithHysteresis(playerId, rawHandYaw, bodyYaw, leftArm);

        // Hand orientation as a quaternion in world / room space.
        Matrix4fc rotMat = handPose.getRotation();
        Quaternionf qHandWorld = rotMat.getNormalizedRotation(new Quaternionf()).normalize();

        // Express the hand orientation in the body's local frame: cancel out the body yaw.
        Quaternionf qHandBody = new Quaternionf().rotateY(-bodyYaw).mul(qHandWorld);

        // Inverse of the arm's local rotation. ModelPart applies rotations as
        //   q_arm = rotationZYX(0, armYawDelta, -PI/2 - armPitch)
        //         = R_x(-PI/2 - armPitch) · R_y(armYawDelta)
        // so the inverse is
        //   q_arm⁻¹ = R_y(-armYawDelta) · R_x( PI/2 + armPitch).
        Quaternionf qArmInv = new Quaternionf()
                .rotateY(-armYawDelta)
                .rotateX(Mth.HALF_PI + armPitch);

        // Residual rotation in the arm's local frame at the wrist. After the arm
        // rotation has been applied, local Y points along the arm direction, so a
        // rotation around local Y is the wrist twist; rotation in the X-Z plane is
        // wrist flex/extension and deviation combined.
        Quaternionf qResidual = qArmInv.mul(qHandBody, new Quaternionf());

        // Canonicalize: ensure w >= 0 so atan2 gives the short-way angle.
        if (qResidual.w < 0f) {
            qResidual.set(-qResidual.x, -qResidual.y, -qResidual.z, -qResidual.w);
        }

        // Swing-twist decomposition along local Y.
        Quaternionf qTwist = new Quaternionf();
        Quaternionf qSwing = new Quaternionf();
        decomposeSwingTwistY(qResidual, qSwing, qTwist);

        // Clamp the wrist twist (pronation / supination).
        float twistAngle = wrapToPi(2.0f * (float) Math.atan2(qTwist.y, qTwist.w));
        float clampedTwist = Mth.clamp(twistAngle, -WRIST_TWIST_LIMIT, WRIST_TWIST_LIMIT);
        float halfTwist = clampedTwist * 0.5f;
        qTwist.set(0f, (float) Math.sin(halfTwist), 0f, (float) Math.cos(halfTwist));

        // Clamp the swing (combined flex/extend + deviation magnitude).
        clampSwingMagnitude(qSwing, WRIST_SWING_LIMIT);

        // Recombine: residual = swing · twist  (mulPose applies right-to-left, so
        // twist rotates the item around the arm axis first, then swing tilts).
        Quaternionf qFinalResidual = qSwing.mul(qTwist, new Quaternionf());

        return new ArmFrame(armPitch, armYawDelta, qFinalResidual);
    }

    /** Splits {@code q} into {@code q == swingOut · twistOut} where twist's axis is local Y. */
    private static void decomposeSwingTwistY(Quaternionf q, Quaternionf swingOut, Quaternionf twistOut) {
        float qy = q.y;
        float qw = q.w;
        float magSq = qy * qy + qw * qw;
        if (magSq > 1.0e-8f) {
            float invMag = 1.0f / (float) Math.sqrt(magSq);
            twistOut.set(0f, qy * invMag, 0f, qw * invMag);
        } else {
            // Pure 180° rotation in the X-Z plane: pick zero twist.
            twistOut.identity();
        }
        // swing = q · twist⁻¹
        Quaternionf inv = new Quaternionf(-twistOut.x, -twistOut.y, -twistOut.z, twistOut.w);
        swingOut.set(q).mul(inv);
    }

    /**
     * Clamps the rotation angle of a "swing" quaternion (Y component near zero) to a
     * maximum, preserving the rotation axis in the X-Z plane.
     */
    private static void clampSwingMagnitude(Quaternionf swing, float limit) {
        float w = Mth.clamp(swing.w, -1f, 1f);
        float angle = 2.0f * (float) Math.acos(w);
        if (angle <= limit || angle <= 1.0e-6f) {
            return;
        }
        float xzMag = (float) Math.sqrt(swing.x * swing.x + swing.z * swing.z);
        if (xzMag <= 1.0e-6f) {
            return;
        }
        float halfNew = limit * 0.5f;
        float sinHalf = (float) Math.sin(halfNew);
        float cosHalf = (float) Math.cos(halfNew);
        float scale = sinHalf / xzMag;
        swing.set(swing.x * scale, 0f, swing.z * scale, cosHalf);
    }

    public static void forgetPlayer(UUID playerId) {
        STATE.remove(playerId);
    }
}
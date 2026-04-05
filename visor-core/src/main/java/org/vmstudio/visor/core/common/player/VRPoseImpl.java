package org.vmstudio.visor.core.common.player;

import lombok.Getter;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRPose;


@Getter
public class VRPoseImpl implements VRPose {

    private Vector3fc position;

    private Vector3fc relativePosition;

    private Vector3fc direction;

    private Matrix4fc rotation;

    private Matrix4fc invertedRotation;

    private float yaw, pitch, roll;


    private Vector3fc rawPosition;
    private Vector3fc rawDirection;
    private Matrix4fc rawRotation;

    private Vector3fc usedOrigin;
    private float usedRotationY;
    private float usedWorldScale;


    public VRPoseImpl() {
        position = new Vector3f(0, 0, 0);
        relativePosition = new Vector3f(0, 0, 0);
        direction = new Vector3f(0, 0, 0);
        rotation = new Matrix4f();
        invertedRotation = new Matrix4f();

        usedOrigin = new Vector3f(0, 0, 0);

        rawPosition = new Vector3f();
        rawDirection = new Vector3f();
        rawRotation = new Matrix4f();

    }

    @Override
    public void update(@NotNull Vector3fc rawPosition,
                       @NotNull Matrix4fc rawMatrix,
                       @NotNull Vector3fc rawDirection,
                       @NotNull Vector3fc origin,
                       float rotationY,
                       float worldScale) {
        this.usedOrigin = origin;
        this.usedRotationY = rotationY;
        this.usedWorldScale = worldScale;
        this.rawRotation = rawMatrix;
        this.rawPosition = rawPosition;
        this.rawDirection = rawDirection;

        this.rotation = new Matrix4f().rotationY(rotationY).mul(
                rawMatrix,
                new Matrix4f()
        );
        this.invertedRotation = this.rotation.invert(new Matrix4f());


        this.relativePosition = rawPosition
                .mul(worldScale, new Vector3f())
                .rotateY(rotationY);
        this.position = this.relativePosition
                .add(origin, new Vector3f());

        this.direction = rawDirection.rotateY(rotationY, new Vector3f());


        this.yaw = (float) Mth.atan2(-this.direction.x(), this.direction.z());
        this.pitch = (float) Math.asin(this.direction.y() / this.direction.length());
        this.roll = (float) -Math.atan2(rawMatrix.m01(), rawMatrix.m11());


    }

    @Override
    public void updateModifiers(@NotNull Vector3fc newOrigin,
                                float newRotationY,
                                float newWorldScale) {

        boolean rotationYChanged = newRotationY != usedRotationY;
        boolean scaleChanged = newWorldScale != usedWorldScale;
        boolean originChanged = !usedOrigin.equals(newOrigin);

        if (!rotationYChanged && !scaleChanged && !originChanged) {
            return;
        }

        this.usedOrigin = newOrigin;
        this.usedRotationY = newRotationY;
        this.usedWorldScale = newWorldScale;

        if (!rotationYChanged && !scaleChanged) {
            onOriginChanged(newOrigin);
            return;
        }

        Matrix4f yawMat = new Matrix4f().rotationY(newRotationY);
        this.rotation = yawMat.mul(rawRotation, new Matrix4f());

        this.invertedRotation = this.rotation.invert(new Matrix4f());

        this.relativePosition = rawPosition
                .mul(newWorldScale, new Vector3f())
                .rotateY(newRotationY);

        this.position = this.relativePosition
                .add(newOrigin, new Vector3f());

        this.direction = rawDirection.rotateY(newRotationY, new Vector3f());

        this.yaw = (float) Mth.atan2(-this.direction.x(), this.direction.z());

        float len = this.direction.length();
        if (len > 1e-6f) {
            this.pitch = (float) Math.asin(this.direction.y() / len);
        } else {
            this.pitch = 0f;
        }
        this.roll = (float) -Mth.atan2(rawRotation.m10(), rawRotation.m11());
    }

    public void copyFrom(@NotNull VRPose pose) {
        this.usedOrigin = new Vector3f(pose.getUsedOrigin());
        this.usedRotationY = pose.getUsedRotationY();
        this.usedWorldScale = pose.getUsedWorldScale();
        this.rawRotation = new Matrix4f(pose.getRawRotation());
        this.rawPosition = new Vector3f(pose.getRawPosition());
        this.rawDirection = new Vector3f(pose.getRawDirection());

        this.position = new Vector3f(pose.getPosition());
        this.relativePosition = new Vector3f(pose.getRelativePosition());
        this.direction = new Vector3f(pose.getDirection());
        this.rotation = new Matrix4f(pose.getRotation());
        this.invertedRotation = new Matrix4f(pose.getInvertedRotation());

        this.yaw = pose.getYaw();
        this.pitch = pose.getPitch();
        this.roll = pose.getRoll();
    }

    public void onOriginChanged(Vector3fc newOrigin) {
        this.position = this.relativePosition.add(newOrigin, new Vector3f());
        this.usedOrigin = newOrigin;
    }


    @Override
    public @NotNull Vector3f getCustomVector(@NotNull Vector3fc vec) {
        return this.rotation
                .transformDirection(
                        vec.x(), vec.y(), vec.z(),
                        new Vector3f()
                );
    }

    @Override
    public @NotNull Vector3f reverseCustomVector(@NotNull Vector3fc vec) {

        return this.invertedRotation
                .transformDirection(
                        vec.x(), vec.y(), vec.z(),
                        new Vector3f()
                );
    }


    public Vector3f getScaledPosDelta(float rotationY,
                                      float oldWorldScale,
                                      float newWorldScale) {
        Vector3f oldPos = rawPosition.mul(oldWorldScale, new Vector3f())
                .rotateY(rotationY);
        Vector3f newPos = rawPosition.mul(newWorldScale, new Vector3f())
                .rotateY(rotationY);
        return newPos.sub(oldPos);
    }


    @Override
    public String toString() {
        return String.format(
                "VRPose [position=%s, direction=%s,  yaw=%.2f°, pitch=%.2f°, roll=%.2f°]",
                getPosition(), getDirection(), yaw, pitch, roll
        );
    }
}

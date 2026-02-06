package me.phoenixra.visor.core.common.player;

import lombok.Getter;
import me.phoenixra.visor.api.common.player.VRPose;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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

    private Vector3fc originCache;
    private float rotationYCache;
    private float worldScaleCache;


    public VRPoseImpl() {
        position = new Vector3f(0, 0, 0);
        relativePosition = new Vector3f(0, 0, 0);
        direction = new Vector3f(0, 0, 0);
        rotation = new Matrix4f();
        invertedRotation = new Matrix4f();

        originCache = new Vector3f(0, 0, 0);

        rawPosition = new Vector3f();
        rawDirection = new Vector3f();
        rawRotation = new Matrix4f();

    }

    public void update(Vector3fc origin,
                       float rotationY,
                       float worldScale,
                       Vector3fc position,
                       Matrix4fc rotationMatrix,
                       Vector3fc direction) {
        this.originCache = origin;
        this.rotationYCache = rotationY;
        this.worldScaleCache = worldScale;
        this.rawRotation = rotationMatrix;
        this.rawPosition = position;
        this.rawDirection = direction;

        this.rotation = new Matrix4f().rotationY(rotationY).mul(
                rotationMatrix,
                new Matrix4f()
        );
        this.invertedRotation = this.rotation.invert(new Matrix4f());


        this.relativePosition = position
                .mul(worldScale, new Vector3f())
                .rotateY(rotationY);
        this.position = this.relativePosition
                .add(origin, new Vector3f());

        this.direction = direction.rotateY(rotationY, new Vector3f());


        this.yaw = (float) Mth.atan2(-this.direction.x(), this.direction.z());
        this.pitch = (float) Math.asin(this.direction.y() / this.direction.length());
        this.roll = (float) -Math.atan2(rotationMatrix.m01(), rotationMatrix.m11());


    }

    public void update(Vector3fc newOrigin,
                       float newRotationY,
                       float newWorldScale) {

        boolean yawChanged = newRotationY != rotationYCache;
        boolean scaleChanged = newWorldScale != worldScaleCache;
        boolean originChanged = !originCache.equals(newOrigin);

        if (!yawChanged && !scaleChanged && !originChanged) {
            return;
        }

        this.originCache = newOrigin;
        this.rotationYCache = newRotationY;
        this.worldScaleCache = newWorldScale;

        if (!yawChanged && !scaleChanged) {
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

    public void copyFrom(VRPoseImpl element) {
        this.originCache = new Vector3f(element.originCache);
        this.rotationYCache = element.rotationYCache;
        this.worldScaleCache = element.worldScaleCache;
        this.rawRotation = new Matrix4f(element.rawRotation);
        this.rawPosition = new Vector3f(element.rawPosition);
        this.rawDirection = new Vector3f(element.rawDirection);

        this.position = new Vector3f(element.position);
        this.relativePosition = new Vector3f(element.relativePosition);
        this.direction = new Vector3f(element.direction);
        this.rotation = new Matrix4f(element.rotation);
        this.invertedRotation = new Matrix4f(element.invertedRotation);

        this.yaw = element.yaw;
        this.pitch = element.pitch;
        this.roll = element.roll;

    }

    public void onOriginChanged(Vector3fc newOrigin) {
        this.position = this.relativePosition.add(newOrigin, new Vector3f());
        this.originCache = newOrigin;
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


    public Vector3f getScalePosOffset(float rotaionY, float oldWorldScale, float newWorldScale) {
        Vector3f oldPos = position.mul(oldWorldScale, new Vector3f())
                .rotateY(rotaionY);
        Vector3f newPos = position.mul(newWorldScale, new Vector3f())
                .rotateY(rotaionY);
        return newPos.sub(oldPos);
    }


    @Override
    public String toString() {
        return String.format(
                "VRPoseElement [position=%s, direction=%s,  yaw=%.2f°, pitch=%.2f°, roll=%.2f°]",
                getPosition(), getDirection(), yaw, pitch, roll
        );
    }
}

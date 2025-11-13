package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseElement;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Getter
public class PoseElementImpl implements PoseElement {

    private Vector3fc position;

    private Vector3fc direction;

    private Matrix4fc rotation;

    private Matrix4fc invertedRotation;

    private float yaw, pitch, roll;



    private Vector3fc originCached;


    public PoseElementImpl() {
        position = new Vector3f(0,0,0);
        direction = new Vector3f(0,0,0);
        rotation = new Matrix4f();
        invertedRotation = new Matrix4f();

        originCached = new Vector3f(0,0,0);

    }

    public PoseElementImpl(Vector3fc origin,
                           float rotationY,
                           float worldScale,
                           Matrix4fc rotationMatrix,
                           Vector3fc position, Vector3fc direction
    ) {
        update(
                origin,
                rotationY,
                worldScale,
                rotationMatrix,
                position,
                direction
        );
    }

    protected void update(Vector3fc origin,
                          float rotationY,
                          float worldScale,
                          Matrix4fc rotationMatrix,
                          Vector3fc position, Vector3fc direction){
        this.originCached = origin;

        this.rotation = new Matrix4f().rotationY(rotationY).mul(
                rotationMatrix,
                new Matrix4f()
        );
        this.invertedRotation = this.rotation.invert(new Matrix4f());


        this.position = position
                .mul(worldScale, new Vector3f())
                .rotateY(rotationY)
                .add(origin);


        this.direction = direction.rotateY(rotationY, new Vector3f());


        this.yaw =  (float) Math.toDegrees(
                Mth.atan2(-this.direction.x(), this.direction.z())
        );
        this.pitch = (float) Math.toDegrees(
                Math.asin(this.direction.y() / this.direction.length())
        );

        //here rotationMatrix used instead of rotation, to calculate
        //roll without rotationY affected
        this.roll = (float) (
                -Math.toDegrees(Mth.atan2(rotationMatrix.m10(),
                        rotationMatrix.m11()))
        );


    }




    protected void onOriginChanged(Vector3fc newOrigin){
        this.position = this.position
                .add(
                        newOrigin.x() - originCached.x(),
                        newOrigin.y() - originCached.y(),
                        newOrigin.z() - originCached.z(),
                        new Vector3f()
                );

        originCached = newOrigin;
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

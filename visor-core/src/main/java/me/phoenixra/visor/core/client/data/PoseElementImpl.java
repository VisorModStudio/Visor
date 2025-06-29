package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
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

    private float yaw, pitch, roll;



    private Vector3fc originCached;


    public PoseElementImpl() {
        position = new Vector3f(0,0,0);
        direction = new Vector3f(0,0,0);
        rotation = new Matrix4f();

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
        this.roll = (float) (
                -Math.toDegrees(Mth.atan2(this.rotation.m10(),
                        this.rotation.m11()))
        );


    }




    protected void onOriginChanged(Vector3fc origin){
        this.position = this.position
                .add(
                        origin.x() - originCached.x(),
                        origin.y() - originCached.y(),
                        origin.z() - originCached.z(),
                        new Vector3f()
                );

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

        return this.rotation.invert(new Matrix4f())
                .transformDirection(
                        vec.x(), vec.y(), vec.z(),
                        new Vector3f()
                );
    }


    @Override
    public String toString() {
        return String.format(
                "VRPoseElement [position=%s, direction=%s,  yaw=%.2f°, pitch=%.2f°, roll=%.2f°]",
                getPosition(), getDirection(), yaw, pitch, roll
        );
    }
}

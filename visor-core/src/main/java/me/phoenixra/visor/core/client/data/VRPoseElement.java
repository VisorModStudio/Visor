package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

@Getter
public class VRPoseElement implements PoseElement {

    private Vec3 position;

    private Vec3 direction;

    private Matrix4f rotationMatrix;

    private float yaw, pitch, roll;



    private Vec3 originCached;


    public VRPoseElement() {
        position = new Vec3(0,0,0);
        direction = new Vec3(0,0,0);
        rotationMatrix = new Matrix4f();

        originCached = new Vec3(0,0,0);
    }

    public VRPoseElement(Vec3 origin,
                         float rotationY,
                         float worldScale,
                         Matrix4fc rotationMatrix,
                         Vec3 position, Vec3 direction
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


    public @NotNull Matrix4fc getRotationMatrix() {
        return rotationMatrix;
    }

    protected void update(Vec3 origin,
                          float rotationY,
                          float worldScale,
                          Matrix4fc rotationMatrix,
                          Vec3 position, Vec3 direction){
        this.originCached = origin;

        this.rotationMatrix = new Matrix4f().rotationY(rotationY).mul(
                rotationMatrix,
                new Matrix4f()
        );


        this.position = position
                .scale(worldScale)
                .yRot(rotationY)
                .add(origin.x, origin.y, origin.z);

        this.direction = direction.yRot(rotationY);

        this.yaw =  (float) Math.toDegrees(
                Mth.atan2(-this.direction.x, this.direction.z)
        );
        this.pitch = (float) Math.toDegrees(
                Math.asin(this.direction.y / this.direction.length())
        );
        this.roll = (float) (
                -Math.toDegrees(Mth.atan2(this.rotationMatrix.m10(),
                        this.rotationMatrix.m11()))
        );

    }




    protected void onOriginChanged(Vec3 origin){
        this.position = this.position
                .add(
                        origin.x - originCached.x,
                        origin.y - originCached.y,
                        origin.z - originCached.z
                );
    }


    @Override
    public @NotNull Vec3 getCustomVector(@NotNull Vector3f vec) {
        Vector3f transform = this.rotationMatrix
                .transformDirection(
                        new Vector3f(
                                vec.x,
                                vec.y,
                                vec.z
                        ),
                        new Vector3f()
                );
        return VRMathUtils.convertToMcVector(transform);
    }

    @Override
    public @NotNull Vector3f reverseCustomVector(@NotNull Vec3 customVec) {

        return this.rotationMatrix.invert(new Matrix4f())
                .transformDirection(
                        new Vector3f(
                                (float) customVec.x,
                                (float) customVec.y,
                                (float) customVec.z
                        ),
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

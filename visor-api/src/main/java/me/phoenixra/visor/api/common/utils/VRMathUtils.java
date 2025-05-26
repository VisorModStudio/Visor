package me.phoenixra.visor.api.common.utils;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VRMathUtils {
    public static final Vector3fc forwardVector = new Vector3f(0.0F, 0.0F, -1.0F);
    public static final Vector3fc forwardVectorReversed = new Vector3f(0.0F, 0.0F, 1.0F);
    public static final Vector3fc upVector = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3fc rightVector = new Vector3f(1.0F, 0.0F, 0.0F);

    public static final Vec3 forwardVectorMc = new Vec3(0.0F, 0.0F, -1.0F);
    public static final Vec3 upVectorMc = new Vec3(0.0F, 1.0F, 0.0F);
    public static final Vec3 downVector = new Vec3(0.0D, -1.0D, 0.0D);



    public static Vec3 lerpVector(Vec3 start, Vec3 end, double stepScale) {
        double d0 = start.x + (end.x - start.x) * stepScale;
        double d1 = start.y + (end.y - start.y) * stepScale;
        double d2 = start.z + (end.z - start.z) * stepScale;
        return new Vec3(d0, d1, d2);
    }

    public static Vec3 convertToMcVector(Vector3f vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }


}

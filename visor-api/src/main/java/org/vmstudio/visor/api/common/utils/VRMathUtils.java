package org.vmstudio.visor.api.common.utils;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import redempt.crunch.Crunch;

public class VRMathUtils {

    private VRMathUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static final Vector3fc ZERO_VECTOR = new Vector3f(0,0,0);
    public static final Vector3fc FORWARD_VECTOR = new Vector3f(0.0F, 0.0F, 1.0F);
    public static final Vector3fc BACK_VECTOR = new Vector3f(0.0F, 0.0F, -1.0F);
    public static final Vector3fc UP_VECTOR = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3fc DOWN_VECTOR = new Vector3f(0.0F, -1.0F, 0.0F);

    public static final Vector3fc LEFT_VECTOR = new Vector3f(1.0F, 0.0F, 0.0F);
    public static final Vector3fc RIGHT_VECTOR = new Vector3f(1.0F, 0.0F, 0.0F);

    public static final Vector3fc UNIT_VECTOR = new Vector3f(1.0F, 1.0F, 1.0F);

    public static final Matrix4fc EMPTY_MATRIX = new Matrix4f();

    public static @NotNull Vector3f extractUpDir(@NotNull Matrix4fc rotation, boolean normalize) {
        var out = new Vector3f(rotation.m10(), rotation.m11(), rotation.m12());
        Vector3fc vec = new Vector3f();

        new Vec3((Vector3f) vec);
        return normalize ? out.normalize() : out;
    }

    public static @NotNull Vector3f extractForwardDir(@NotNull Matrix4fc rotation, boolean normalize) {
        var out = new Vector3f(-rotation.m20(), -rotation.m21(), -rotation.m22());
        return normalize ? out.normalize() : out;
    }
    public static @NotNull Vector3f extractRightDir(@NotNull Matrix4fc rotation, boolean normalize) {
        Vector3f v = new Vector3f(rotation.m00(), rotation.m01(), rotation.m02());
        return normalize ? v.normalize() : v;
    }


    public static double getEvaluated(ConfigManager configManager, String formula){
        var placeholderHandler = configManager.getPlaceholderHandler();
        if(placeholderHandler.isEmpty()){
            throw new RuntimeException("Tried to evaluate expression with configManager that lacks placeholderHandler");
        }

        var env = ((AtumConfigManager)configManager).getEvaluationEnvironment();
        return Crunch.compileExpression(
                placeholderHandler
                        .orElse(PlaceholderHandler.EMPTY)
                        .translatePlaceholders(
                                formula
                        ),
               env
        ).evaluate();
    }
    public static Vector3f lerpVector(Vector3fc start, Vector3fc end, float stepScale) {
        float d0 = start.x() + (end.x() - start.x()) * stepScale;
        float d1 = start.y() + (end.y() - start.y()) * stepScale;
        float d2 = start.z() + (end.z() - start.z()) * stepScale;
        return new Vector3f(d0, d1, d2);
    }


}

package me.phoenixra.visor.api.common.utils;

import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import redempt.crunch.Crunch;

public class VRMathUtils {
    public static final Vector3fc forwardVector = new Vector3f(0.0F, 0.0F, -1.0F);
    public static final Vector3fc forwardVectorReversed = new Vector3f(0.0F, 0.0F, 1.0F);
    public static final Vector3fc upVector = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3fc rightVector = new Vector3f(1.0F, 0.0F, 0.0F);

    public static final Vec3 forwardVectorMc = new Vec3(0.0F, 0.0F, -1.0F);
    public static final Vec3 upVectorMc = new Vec3(0.0F, 1.0F, 0.0F);
    public static final Vec3 downVector = new Vec3(0.0D, -1.0D, 0.0D);



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

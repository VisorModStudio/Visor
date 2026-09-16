package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Cross-mc-version facade over entity
 */
public class McEntity {
    private McEntity() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void snapTo(Entity entity, Vec3 position) {
        entity.moveTo(position);
    }

    public static void absSnapTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        entity.absMoveTo(x, y, z, yRot, xRot);
    }

    public static double fallDistance(Entity entity) {
        return entity.fallDistance;
    }

    public static boolean isLocallyControlled(Entity entity) {
        return entity.isControlledByLocalInstance();
    }
}

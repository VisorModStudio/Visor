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
        //? if >=1.21.5 {
        entity.snapTo(position);
        //?} else {
        /*entity.moveTo(position);
        *///?}
    }

    public static void absSnapTo(Entity entity, double x, double y, double z, float yRot, float xRot) {
        //? if >=1.21.5 {
        entity.absSnapTo(x, y, z, yRot, xRot);
        //?} else {
        /*entity.absMoveTo(x, y, z, yRot, xRot);
        *///?}
    }

    public static double fallDistance(Entity entity) {
        return entity.fallDistance;
    }

    public static boolean isLocallyControlled(Entity entity) {
        //? if >=1.21.5 {
        return entity.isLocalInstanceAuthoritative();
        //?} else {
        /*return entity.isControlledByLocalInstance();
        *///?}
    }
}

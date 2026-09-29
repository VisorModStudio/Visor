package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
//?}

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

    public static ServerLevel serverLevel(ServerPlayer player) {
        //? if >=1.21.6 {
        return player.level();
        //?} else {
        /*return player.serverLevel();
        *///?}
    }

    // 1.21.11 turned the permission level into a PermissionSet
    public static boolean hasPermissions(Player player, int level) {
        //? if >=1.21.11 {
        return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
        //?} else {
        /*return player.hasPermissions(level);
        *///?}
    }

    public static boolean isLocallyControlled(Entity entity) {
        //? if >=1.21.5 {
        return entity.isLocalInstanceAuthoritative();
        //?} else {
        /*return entity.isControlledByLocalInstance();
        *///?}
    }
}

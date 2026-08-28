package org.vmstudio.visor.core.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

import java.util.function.Supplier;

public class CommonUtils {

    public static final ThreadLocal<ItemStack> FORCED_HAND_ITEM = new ThreadLocal<>();

    public static <T> T withForcedHand(ItemStack item, Supplier<T> action) {
        FORCED_HAND_ITEM.set(item);
        try { return action.get(); }
        finally { FORCED_HAND_ITEM.remove(); }
    }

    public static void withForcedHand(ItemStack item, Runnable action) {
        FORCED_HAND_ITEM.set(item);
        try { action.run(); }
        finally { FORCED_HAND_ITEM.remove(); }
    }






    public static @Nullable Vec3 calcVRKnockback(@Nullable Entity attacker, Entity target) {
        if (!(attacker instanceof ServerPlayer serverPlayer)
                || VisorAPI.server() == null) {
            return null;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(serverPlayer);
        if (vrPlayer == null) {
            return null;
        }
        double x = serverPlayer.getX() - target.getX();
        double z = serverPlayer.getZ() - target.getZ();
        if (x * x + z * z >= 1.0E-4d) {
            return new Vec3(x, 0.0, z).normalize();
        }
        //Edge case: when entities too close, use hmdDir instead
        if (vrPlayer.hasPoseData()) {
            Vector3fc hmdDir = vrPlayer.getPoseData().getHmd().getDirection();
            double hx = hmdDir.x();
            double hz = hmdDir.z();
            if (hx * hx + hz * hz >= 1.0E-4d) {
                return new Vec3(-hx, 0.0, -hz).normalize();
            }
        }
        return null;
    }


    public static boolean hasInteractableBlock(Level level, AABB box, int blockY) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX - 1.0E-7D);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ - 1.0E-7D);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, blockY, z);
                BlockState state = level.getBlockState(pos);
                if (state.getMenuProvider(level, pos) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasInteractableBlockAhead(Level level,
                                                    AABB playerBox,
                                                    Vec3 motion,
                                                    double distance) {
        double speedSq = motion.x * motion.x + motion.z * motion.z;
        if (speedSq < 1.0E-7D) {
            return false;
        }
        double speed = Math.sqrt(speedSq);
        double dx = motion.x / speed * distance;
        double dz = motion.z / speed * distance;

        AABB projected = playerBox.move(dx, 0.0D, dz);
        return hasInteractableBlock(level, projected, Mth.floor(playerBox.minY));
    }
}

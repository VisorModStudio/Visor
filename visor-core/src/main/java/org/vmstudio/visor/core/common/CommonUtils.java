package org.vmstudio.visor.core.common;

import net.minecraft.core.BlockPos;
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



    public static AABB getEntityHeadHitBox(Entity entity, double inflate) {
        if ((entity instanceof Player player && !player.isSwimming()) || // swimming players hitbox is just a box around their butt
                entity instanceof Zombie ||
                entity instanceof AbstractPiglin ||
                entity instanceof AbstractSkeleton ||
                entity instanceof Witch ||
                entity instanceof AbstractIllager ||
                entity instanceof Blaze ||
                entity instanceof Creeper ||
                entity instanceof EnderMan ||
                entity instanceof AbstractVillager ||
                entity instanceof SnowGolem ||
                entity instanceof Vex ||
                entity instanceof Strider) {

            Vec3 headpos = entity.getEyePosition();
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            return new AABB(headpos.subtract(headsize, headsize - inflate, headsize), headpos.add(headsize, headsize + inflate, headsize)).inflate(inflate);
        } else if (!(entity instanceof EnderDragon) // no ender dragon, the code doesn't work for it
                && entity instanceof LivingEntity livingEntity) {

            float yrot = -(livingEntity.yBodyRot) * Mth.DEG_TO_RAD;
            // offset head in entity rotation
            Vec3 headpos = entity.getEyePosition()
                    .add(new Vec3(Mth.sin(yrot), 0, Mth.cos(yrot))
                            .scale(livingEntity.getBbWidth() * 0.5F));

            double headsize = livingEntity.getBbWidth() * 0.25;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5;
            }
            return new AABB(headpos.subtract(headsize, headsize, headsize), headpos.add(headsize, headsize, headsize)).inflate(inflate * 0.25).expandTowards(headpos.subtract(entity.position()).scale(inflate));
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

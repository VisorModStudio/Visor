package org.vmstudio.visor.core.client.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import org.vmstudio.visor.core.client.ClientContext;

public final class RoomTasksUtils {
    public static final float CHEST_DROP = 0.3f;

    private RoomTasksUtils() {
    }

    public static boolean isBodyFree(LocalPlayer player) {
        return player.isAlive()
                && !player.isPassenger()
                && !player.isSleeping()
                && !player.isSpectator()
                && !player.getAbilities().flying
                && !player.isFallFlying()
                && !player.isAutoSpinAttack();
    }

    public static boolean canSneakOrCrawl(LocalPlayer player) {
        return isBodyFree(player)
                && !isSubmerged(player)
                && !ClientContext.localPlayer.isClimbing();
    }

    public static boolean isSubmerged(LocalPlayer player) {
        if (!player.isInWater() && !player.isInLava()) {
            return false;
        }
        BlockPos body = player.blockPosition();
        return hasSwimFluid(player, body) && hasSwimFluid(player, body.above());
    }

    private static boolean hasSwimFluid(LocalPlayer player, BlockPos pos) {
        var fluid = player.level().getFluidState(pos);
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }
}

package org.vmstudio.visor.extensions.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface ServerPlayerGameModeExtension {
    void visor$handleVrBlockDamage(BlockPos blockPos,
                                   Direction direction,
                                   int i, int j,
                                   ItemStack usedItem);
}

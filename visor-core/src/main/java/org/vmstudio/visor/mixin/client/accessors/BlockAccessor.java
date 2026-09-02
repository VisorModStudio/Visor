package org.vmstudio.visor.mixin.client.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {

    @Invoker("spawnDestroyParticles")
    void visor$spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state);
}

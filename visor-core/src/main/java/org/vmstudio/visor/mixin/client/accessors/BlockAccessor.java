package org.vmstudio.visor.mixin.client.accessors;

import net.minecraft.core.BlockPos;
//? if >=26.3 {
import net.minecraft.world.entity.Entity;
//?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {

    //? if >=26.3 {
    @Invoker("spawnDestroyByEntityParticles")
    void visor$spawnDestroyParticles(Level level, Entity entity, BlockPos pos, BlockState state);
    //?} else {
    /*@Invoker("spawnDestroyParticles")
    void visor$spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state);
    *///?}
}

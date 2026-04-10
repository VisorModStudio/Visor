package org.vmstudio.visor.extensions.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface LevelRendererExtension {
    Entity visor$getRenderedEntity();

    void visor$damageBlockProgress(@NotNull Player player,
                                   @NotNull BlockPos blockPos,
                                   int destroyStage);
}

package org.vmstudio.visor.modified.client.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface LevelRendererSwingingModified {
    void visor$damageBlockProgress(@NotNull Player player,
                                  @NotNull BlockPos blockPos,
                                  int destroyStage);
}

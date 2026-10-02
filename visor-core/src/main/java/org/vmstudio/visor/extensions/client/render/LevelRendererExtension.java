package org.vmstudio.visor.extensions.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface LevelRendererExtension {
    static LevelRendererExtension get() {
        //? if >=26.2 {
        return (LevelRendererExtension) Minecraft.getInstance().levelExtractor;
        //?} else {
        /*return (LevelRendererExtension) Minecraft.getInstance().levelRenderer;
        *///?}
    }

    Entity visor$getCurrentRenderEntity();

    void visor$damageBlockProgress(@NotNull Player player,
                                   @NotNull BlockPos blockPos,
                                   int destroyStage);
}

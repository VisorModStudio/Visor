package org.vmstudio.visor.api.server.player;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.player.VisorPlayer;


public interface VisorServerPlayer extends VisorPlayer {

    @NotNull
    ServerPlayer getMcPlayer();

    @Override
    default @Nullable VRServerPlayer asVR() {
        return this instanceof VRServerPlayer vr ? vr : null;
    }
}

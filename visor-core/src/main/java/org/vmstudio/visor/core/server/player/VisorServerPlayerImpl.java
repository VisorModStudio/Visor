package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.server.player.VisorServerPlayer;

//other players data receiver
public class VisorServerPlayerImpl implements VisorServerPlayer {
    @Getter @Setter
    protected @NotNull ServerPlayer mcPlayer;

    public VisorServerPlayerImpl(@NotNull ServerPlayer mcPlayer){
        this.mcPlayer = mcPlayer;
    }
}

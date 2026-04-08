package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

//other players data receiver
public class VisorPacketReceiver {
    @Getter @Setter
    protected @NotNull ServerPlayer mcPlayer;

    public VisorPacketReceiver(@NotNull ServerPlayer mcPlayer){
        this.mcPlayer = mcPlayer;
    }
}

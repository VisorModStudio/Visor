package org.vmstudio.visor.mixin.common.listeners;

import org.vmstudio.visor.core.server.network.ServerNetworking;
import org.vmstudio.visor.core.server.VisorServerImpl;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ServerListenerMixins {
    @Mixin(ServerGamePacketListenerImpl.class)
    public abstract static class ServerListenerMixin  implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {

        @Shadow
        public ServerPlayer player;



        @Inject(at = @At("TAIL"), method = "tick()V")
        public void visor$onTick(CallbackInfo info) {
            ServerNetworking.sendVRStatePacketOf(this.player);
        }

        @Inject(at = @At("TAIL"), method = "onDisconnect")
        public void visor$onPlayerLeave(Component component, CallbackInfo ci) {
            // remove player from vr player list, when they leave

            VisorServerImpl.INSTANCE.getPlayersWithVR()
                    .remove(this.player.getUUID());
        }
    }
}

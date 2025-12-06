package me.phoenixra.visor.mixin.common.listeners;

import me.phoenixra.visor.api.server.VRServerSettings;
import me.phoenixra.visor.core.server.network.ServerNetworking;
import me.phoenixra.visor.core.server.VisorServerImpl;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PlayerListenerMixins {
    @Mixin(PlayerList.class)
    public static class PlayerListMixin {

        @Inject(at = @At("HEAD"), method = "placeNewPlayer")
        private void visor$onLogin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
            if (VRServerSettings.isVrOnly()){
                ServerNetworking.kickDelayedIfNoVR(serverPlayer);
            }
        }
        @Redirect(method = "respawn", at = @At(value = "INVOKE",
                target = "Lnet/minecraft/server/level/ServerPlayer;initInventoryMenu()V"))
        private void visor$onPlayerRespawn(ServerPlayer serverPlayer) {
            VisorServerImpl.INSTANCE.updateVrPlayer(serverPlayer);
            serverPlayer.initInventoryMenu();
        }
    }
}

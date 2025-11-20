package me.phoenixra.visor.mixin.client.multiplayer;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.VisorNetwork;
import me.phoenixra.visor.api.common.network.toserver.HandshakePayloadToServer;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.network.ClientNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {


    @Inject(method = "<init>", at = @At("TAIL"))
    private void visor$init(CallbackInfo ci) {
        ClientNetworking.dispose();
    }


    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void visor$onLogin(CallbackInfo ci) {
        ClientNetworking.dispose();
        ClientNetworking.sendHandShake(
                new HandshakePayloadToServer(
                        VisorState.getState().isActive(),
                        VisorNetwork.NETWORK_VERSION,
                        ModLoader.get().getModVersion(VisorAPI.MOD_ID)
                )
        );
    }
    @Inject(method = "close", at = @At("TAIL"))
    private void visor$cleanup(CallbackInfo ci) {
        ClientNetworking.dispose();
    }
}

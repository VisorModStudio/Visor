package org.vmstudio.visor.mixin.client.multiplayer;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.common.network.VisorNetwork;
import org.vmstudio.visor.api.common.network.toserver.HandshakePayloadToServer;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

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
                        VRClientSettings.getVrPlayMode() != VRPlayMode.DISABLED,
                        VisorNetwork.CORE_NETWORK_VERSION,
                        ModLoader.get().getModVersion(VisorAPI.MOD_ID)
                )
        );
    }
    @Inject(method = "close", at = @At("TAIL"))
    private void visor$cleanup(CallbackInfo ci) {
        ClientNetworking.dispose();
    }
}

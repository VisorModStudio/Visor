package me.phoenixra.visor.core.common.network.client;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.toclient.*;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VRActivePayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VRStatePayloadToClient;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import me.phoenixra.visor.core.server.ServerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientPacketHandler {
    public static boolean displayedServerStatusMsg = false;


    public static void handlePacket(VisorPayloadToClient payloadClient){
        if (payloadClient instanceof UnknownPayloadToClient) return;

        Minecraft mc = Minecraft.getInstance();
        switch (payloadClient.payloadId()) {
            case HANDSHAKE -> {
                var payload = (HandshakePayloadToClient) payloadClient;
                if (!displayedServerStatusMsg
                        && !Minecraft.getInstance().isLocalServer()) {
                    displayedServerStatusMsg = true;
                    mc.gui.getChat().addMessage(
                            Component.translatable(
                                    "visor.messages.server_mod",
                                    payload.networkVersion()
                            )
                    );
                }
                if (VisorState.getState().isActive()
                        && VRClientSettings.getPlayerHeight() == -1.0F) {
                    mc.gui.getChat().addMessage(
                            Component.translatable("visor.messages.calibrate_height")
                    );
                }
                ClientNetworking.SERVER_HAS_VISOR = true;
            }
            case SETTINGS -> {
                var payload = (SettingsPayloadToClient) payloadClient;

                ServerConfig.updateServerSettings(
                        VisorAPI.client().getConfigManager(),
                        payload.config()
                );
            }
            case PLAYER_VR_ACTIVE -> {
                var payload = (VRActivePayloadToClient) payloadClient;
                if (!payload.hasVr()) {
                    VRRemotePlayers.getInstance()
                            .removePlayer(payload.playerUUID());
                }
            }
            case PLAYER_VR_STATE -> {
                var payload = (VRStatePayloadToClient) payloadClient;
                VRRemotePlayers.getInstance().applyPlayer(
                        payload.playerUUID(),
                        payload.pose(),
                        payload.worldScale(),
                        payload.heightScale()
                );
            }

        }
    }
}

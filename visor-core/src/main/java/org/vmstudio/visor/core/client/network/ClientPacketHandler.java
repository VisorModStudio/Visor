package org.vmstudio.visor.core.client.network;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.UnknownPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.VROtherActivePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.VROtherStatePayloadToClient;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.common.ServerConfig;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {


    public static void handlePacket(VisorPayloadToClient payloadClient){
        if (payloadClient instanceof UnknownPayloadToClient) return;

        Minecraft mc = Minecraft.getInstance();
        switch (payloadClient.payloadId()) {
            case HANDSHAKE -> {
                ClientNetworking.receivedHandShake();
            }
            case SETTINGS -> {
                var payload = (SettingsPayloadToClient) payloadClient;

                ServerConfig.updateSettings(
                        VisorAPI.client().getConfigManager(),
                        payload.config()
                );
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToClient) payloadClient;
                ClientContext.localPlayer.setRotationY(
                        payload.rotationY()
                );
            }
            case OTHER_VR_ACTIVE -> {
                var payload = (VROtherActivePayloadToClient) payloadClient;
                if (!payload.vrActive()) {
                    VRClientPlayers
                            .removePlayer(payload.playerUUID());
                }
            }
            case OTHER_VR_STATE -> {
                var payload = (VROtherStatePayloadToClient) payloadClient;
                VRClientPlayers.receivedPacket(
                        payload.playerUUID(),
                        payload.pose(),
                        payload.worldScale(),
                        payload.fullHeight()
                );
            }

        }
    }
}

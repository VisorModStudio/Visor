package me.phoenixra.visor.core.client.network;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.common.network.toclient.SettingsPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.UnknownPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VROtherActivePayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VROtherStatePayloadToClient;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.player.VRClientPlayers;
import me.phoenixra.visor.core.client.tasks.movement.TaskInputRotation;
import me.phoenixra.visor.core.server.ServerConfig;
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

                ServerConfig.updateServerSettings(
                        VisorAPI.client().getConfigManager(),
                        payload.config()
                );
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToClient) payloadClient;
                TaskInputRotation.getInstance().setInputRotation(
                        payload.rotationY() - ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getRotationY()
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
                        payload.height()
                );
            }

        }
    }
}

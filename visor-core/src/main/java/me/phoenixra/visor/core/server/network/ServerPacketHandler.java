package me.phoenixra.visor.core.server.network;

import me.phoenixra.visor.api.common.network.VisorNetwork;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toclient.HandshakePayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.SettingsPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VROtherActivePayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.HandshakePayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.UnknownPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.*;
import me.phoenixra.visor.api.server.VRServerSettings;
import me.phoenixra.visor.core.server.ServerConfig;
import me.phoenixra.visor.core.server.VRServerPlayerImpl;
import me.phoenixra.visor.core.server.VisorServerImpl;
import me.phoenixra.visor.modified.common.ServerPlayerModified;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ServerPacketHandler {



    public static void handlePacket(VisorPayloadToServer payloadToServer,
                                    ServerPlayer serverPlayer,
                                    Consumer<VisorPayloadToClient> packetConsumer){
        if (payloadToServer instanceof UnknownPayloadToServer) return;

        VRServerPlayerImpl vrPlayer = VisorServerImpl.INSTANCE.getVrPlayer(serverPlayer);

        if (vrPlayer == null) {
            if(payloadToServer.payloadId() != VisorPayloadID.HANDSHAKE) {
                return;
            }else{
                vrPlayer = new VRServerPlayerImpl(serverPlayer);
            }
        }

        VisorServerImpl.INSTANCE.updateVrPlayer(serverPlayer);

        switch (payloadToServer.payloadId()) {
            case HANDSHAKE -> {
                var payload = (HandshakePayloadToServer) payloadToServer;
                handleHandshake(
                        serverPlayer, vrPlayer,
                        packetConsumer,
                        payload.vrActive(),
                        payload.networkVersion(),
                        payload.visorVersion()
                );
            }
            case VR_ACTIVE -> {
                var payload = (VRActivePayloadToServer) payloadToServer;
                if (vrPlayer.isVr() == payload.vrActive()) {
                    return;
                }
                boolean hasVR = !vrPlayer.isVr();
                vrPlayer.setVr(hasVR);
                if (hasVR) return;
                ServerNetworking.sendPacketToTrackedVRPlayers(
                        serverPlayer,
                        new VROtherActivePayloadToClient(
                                vrPlayer.mcPlayer.getUUID(),
                                vrPlayer.isVr()
                        )
                );
            }
            case POSE_DATA -> {
                var payload = (PoseDataPayloadToServer) payloadToServer;

                vrPlayer.setPlayerPoseBuffer(
                        payload.pose()
                );
            }
            case WORLD_SCALE -> {
                var payload = (WorldScalePayloadToServer) payloadToServer;
                vrPlayer.setWorldScale(payload.worldScale());

            }
            case HEIGHT -> {
                var payload = (HeightPayloadToServer) payloadToServer;
                vrPlayer.setHeight(payload.height());
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToServer) payloadToServer;
                vrPlayer.updateRotationY(payload.rotationY());
            }
        }
    }

    private static void handleHandshake(ServerPlayer player,
                                        VRServerPlayerImpl vrPlayer,
                                        Consumer<VisorPayloadToClient> packetConsumer,
                                        boolean vrActive,
                                        int networkVersion,
                                        String visorVersion){
        Logger logger = VisorServerImpl.INSTANCE.getLogger();

        if (VRServerSettings.isServerDebug()) {
            logger.info(
                    "Visor: player '{}' joined with {}",
                    player.getName().getString(),
                    visorVersion
            );
        }

        // check if client supports a supported version
        if (networkVersion == VisorNetwork.NETWORK_VERSION)
        {
            if (VRServerSettings.isServerDebug()) {
                logger.info("Player {} has supported Visor network version",
                        player.getName().getString(),
                        networkVersion
                );
            }
        } else {
            // unsupported version, send notification, and disregard
            player.connection.disconnect(
                    Component.literal(
                            String.format(
                                    "Your Visor network version is not supported by this server!" +
                                     "\n Your: %s Server: %s",
                                    networkVersion,  VisorNetwork.NETWORK_VERSION
                            )
                    )
            );
            if (VRServerSettings.isServerDebug()) {
                logger.info(
                        """
                                Player {} has unsupported Visor network version...\
                                
                                Player: {} Server: {}\
                                
                                Disconnecting...""",
                        player.getName(),
                        networkVersion,  VisorNetwork.NETWORK_VERSION
                );
            }
            return;
        }
        vrPlayer.setVr(vrActive);

        if (VRServerSettings.isServerDebug()) {
            VisorServerImpl.LOGGER.info(
                    "VR: player '{}' joined with {}",
                    vrPlayer.getMcPlayer().getName().getString(),
                    visorVersion
            );
        }



        VisorServerImpl.INSTANCE.putVrPlayer(vrPlayer);

        packetConsumer.accept(
                new HandshakePayloadToClient()
        );
        packetConsumer.accept(
                new SettingsPayloadToClient(
                        ServerConfig
                                .getSettingsForClient()
                                .toPlaintext()
                )
        );
        packetConsumer.accept(
                new RotationYPayloadToClient(
                        ((ServerPlayerModified)player).visor$getRotationYCached()
                )
        );
    }

}

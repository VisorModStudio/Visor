package org.vmstudio.visor.core.server.network;

import org.vmstudio.visor.api.common.network.VisorNetwork;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.HandshakePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.VROtherActivePayloadToClient;
import org.vmstudio.visor.api.common.network.toserver.HandshakePayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.TeleportMovePayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.UnknownPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;
import org.vmstudio.visor.api.server.SupportedMovement;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.common.ServerConfig;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;
import org.vmstudio.visor.core.server.VisorServerImpl;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
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
                if (vrPlayer.isVRActive() == payload.vrActive()) {
                    return;
                }
                boolean hasVR = !vrPlayer.isVRActive();
                vrPlayer.setVrActive(hasVR);
                if (hasVR) return;
                ServerNetworking.sendPacketToTrackedVRPlayers(
                        serverPlayer,
                        new VROtherActivePayloadToClient(
                                vrPlayer.getMcPlayer().getUUID(),
                                vrPlayer.isVRActive()
                        )
                );
            }
            case POSE_DATA -> {
                var payload = (PoseDataPayloadToServer) payloadToServer;

                vrPlayer.poseUpdateReceived(
                        payload.pose()
                );
            }
            case WORLD_SCALE -> {
                var payload = (WorldScalePayloadToServer) payloadToServer;
                vrPlayer.setWorldScale(payload.worldScale());
            }
            case FULL_HEIGHT -> {
                var payload = (FullHeightPayloadToServer) payloadToServer;
                vrPlayer.setFullHeight(payload.fullHeight());
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToServer) payloadToServer;
                vrPlayer.updateRotationY(payload.rotationY());
            }
            case CRAWLING -> {
                if(!VRServerSettings.isRoomCrawlingSupported()){
                    return;
                }
                var payload = (CrawlingPayloadToServer) payloadToServer;
                vrPlayer.setCrawling(payload.crawling());
            }
            case CLIMBING -> {
                if(!VRServerSettings.isRoomClimbingSupported()){
                    return;
                }
                vrPlayer.getMcPlayer().fallDistance = 0.0F;
            }
            case TELEPORT -> {
                if(VRServerSettings.getSupportedMovement() == SupportedMovement.CONTROLLER){
                    return;
                }
                var payload = (TeleportMovePayloadToServer) payloadToServer;
                ServerPlayer player = vrPlayer.getMcPlayer();
                player.absMoveTo(
                        payload.x(), payload.y(), payload.z(),
                        player.getYRot(),
                        player.getXRot()
                );
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
        vrPlayer.setVrActive(vrActive);

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
                        ((ServerPlayerExtension)player).visor$getRotationYCached()
                )
        );
    }

}

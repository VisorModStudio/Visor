package me.phoenixra.visor.core.common.network.server;

import me.phoenixra.visor.api.common.network.VisorNetwork;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toclient.HandshakePayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.SettingsPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VRActivePayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.*;
import me.phoenixra.visor.api.common.network.toserver.vrstate.*;
import me.phoenixra.visor.api.server.VRServerSettings;
import me.phoenixra.visor.core.server.ServerConfig;
import me.phoenixra.visor.core.server.VRServerPlayerImpl;
import me.phoenixra.visor.core.server.VisorServer;
import me.phoenixra.visor.core.server.mcmodified.PlayerModified;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;

public class ServerPacketHandler {



    public static void handlePacket(VisorPayloadToServer payloadToServer,
                                    ServerPlayer player,
                                    Consumer<VisorPayloadToClient> packetConsumer){
        if (payloadToServer instanceof UnknownPayloadToServer) return;

        VRServerPlayerImpl vrPlayer = VisorServer.INSTANCE.getVrPlayer(player);

        if (vrPlayer == null) {
            if(payloadToServer.payloadId() != VisorPayloadID.HANDSHAKE) {
                return;
            }else{
                vrPlayer = new VRServerPlayerImpl(player);
            }
        }

        VisorServer.INSTANCE.updateVrPlayer(player);

        switch (payloadToServer.payloadId()) {
            case HANDSHAKE -> {
                var payload = (HandshakePayloadToServer) payloadToServer;
                handleHandshake(
                        player, vrPlayer,
                        packetConsumer,
                        payload.version(),
                        payload.vrActive(),
                        payload.maxVersion(),
                        payload.minVersion()
                );
            }
            case PLAYER_VR_ACTIVE -> {
                var payload = (VRActivePayloadToServer) payloadToServer;
                if (vrPlayer.isVr() == payload.hasVr()) {
                    return;
                }
                boolean hasVR = !vrPlayer.isVr();
                vrPlayer.setVr(hasVR);
                if (hasVR) return;
                ServerNetworking.sendPacketToTrackedPlayers(
                        vrPlayer,
                        false,
                        new VRActivePayloadToClient(
                                vrPlayer.mcPlayer.getUUID(),
                                vrPlayer.isVr()
                        )
                );
            }
            case VR_POSE -> {
                var payload = (VRPosePayloadToServer) payloadToServer;

                vrPlayer.setPlayerPoseBuffer(
                        payload.pose()
                );
            }
            case WORLD_SCALE -> {
                var payload = (WorldScalePayloadToServer) payloadToServer;
                vrPlayer.worldScale = payload.worldScale();

            }
            case HEIGHT -> {
                var payload = (HeightPayloadToServer) payloadToServer;
                vrPlayer.heightScale = payload.heightScale();
            }
        }
    }

    private static void handleHandshake(ServerPlayer player,
                                        VRServerPlayerImpl vrPlayer,
                                        Consumer<VisorPayloadToClient> packetConsumer,
                                        String version,
                                        boolean vrActive,
                                        int maxVersion,
                                        int minVersion){
        Logger logger = VisorServer.INSTANCE.getLogger();

        if (VRServerSettings.isServerDebug()) {
            logger.info(
                    "Visor: player '{}' joined with {}",
                    player.getName().getString(),
                    version
            );
        }

        // check if client supports a supported version
        if (VisorNetwork.MIN_NETWORK_VERSION <= maxVersion
                && minVersion <= VisorNetwork.MAX_NETWORK_VERSION)
        {
            vrPlayer.networkVersion = Math.min(maxVersion,
                    VisorNetwork.MAX_NETWORK_VERSION);
            if (VRServerSettings.isServerDebug()) {
                logger.info("Visor: {} networking supported, using version {}",
                        player.getName().getString(),
                        vrPlayer.networkVersion
                );
            }
        } else {
            // unsupported version, send notification, and disregard
            player.sendSystemMessage(
                    Component.literal("Unsupported visor version, VR features will not work")
            );
            if (VRServerSettings.isServerDebug()) {
                logger.info(
                        "Visor: {} networking not supported. client range [{},{}], server range [{},{}]",
                        player.getScoreboardName(),
                        minVersion,
                        maxVersion,
                        VisorNetwork.MIN_NETWORK_VERSION,
                        VisorNetwork.MAX_NETWORK_VERSION
                );
            }
            return;
        }
        vrPlayer.setVr(vrActive);

        if (VRServerSettings.isServerDebug()) {
            VisorServer.LOGGER.info(
                    "VR: player '{}' joined with {}",
                    vrPlayer.getMcPlayer().getName().getString(),
                    version
            );
        }



        VisorServer.INSTANCE.putVrPlayer(vrPlayer);

        packetConsumer.accept(
                new HandshakePayloadToClient(
                        vrPlayer.networkVersion
                )
        );
        packetConsumer.accept(
                new SettingsPayloadToClient(
                        ServerConfig
                                .getSettingsForClient()
                                .toPlaintext()
                )
        );
    }

}

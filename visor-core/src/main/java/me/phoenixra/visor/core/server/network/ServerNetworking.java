package me.phoenixra.visor.core.server.network;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VRStatePayloadToClient;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import me.phoenixra.visor.core.server.VRServerPlayerImpl;
import me.phoenixra.visor.core.server.VisorServerImpl;
import me.phoenixra.visor.mixin.common.accessors.ChunkMapAccessor;
import me.phoenixra.visor.mixin.common.accessors.TrackedEntityAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Pose;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ServerNetworking {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static void sendVRPacketTo(VRServerPlayer vrServerPlayer,
                                      VisorPayloadToClient payload) {
        if (MC.getConnection() == null) return;
        vrServerPlayer.getMcPlayer().connection
                .send(createVRPacket(payload));
    }

    public static Packet<?> createVRPacket(VisorPayloadToClient payload) {
        return ModLoader.get()
                .createPacketToClient(payload);
    }


    public static void kickDelayedNoVR(ServerPlayer serverPlayer) {
        scheduler.schedule(() -> {
            if(serverPlayer.hasDisconnected()){
                return;
            }
            VRServerPlayer vrServerPlayer = VisorAPI.server()
                    .getVrPlayer(serverPlayer);

            if(serverPlayer.server.getPlayerList()
                    .isOp(serverPlayer.getGameProfile())){
                return;
            }

            if (vrServerPlayer == null || !vrServerPlayer.isVr()) {
                serverPlayer.connection.disconnect(
                        Component.literal(
                                "Server For VR player only!"
                        )
                );
            }

        }, 10000L, TimeUnit.MILLISECONDS);
    }


    public static void updatePlayerPose(ServerPlayer player) {
        VRServerPlayer vrServerPlayer = VisorAPI.server()
                .getVrPlayer(player);

        if (vrServerPlayer != null && vrServerPlayer.isVr()
                && vrServerPlayer.isCrawling()) {
            player.setPose(Pose.SWIMMING);
        }
    }




    public static void sendVRStateToOthers(ServerPlayer serverPlayer) {
        Map<UUID, VRServerPlayer> playersWithVR = VisorServerImpl.INSTANCE.getPlayersWithVR();
        VRServerPlayerImpl vrServerPlayer = (VRServerPlayerImpl) playersWithVR.get(serverPlayer.getUUID());
        if (vrServerPlayer == null) {
            return;
        }
        if (vrServerPlayer.mcPlayer == null || vrServerPlayer.mcPlayer.hasDisconnected()) {
            playersWithVR.remove(serverPlayer.getUUID());
        }
        if (!vrServerPlayer.isVr() || vrServerPlayer.getPlayerPoseBuffer() == null) {
            return;
        }
        sendPacketToTrackedPlayers(
                vrServerPlayer,
                false,
                new VRStatePayloadToClient(
                        vrServerPlayer.mcPlayer.getUUID(),
                        vrServerPlayer.getPlayerPoseBuffer(),
                        vrServerPlayer.worldScale,
                        vrServerPlayer.heightScale
                )
        );
    }



    public static void sendPacketToTrackedPlayers(VRServerPlayer packetOwner,
                                                  boolean sendToOwner,
                                                  VisorPayloadToClient payload) {
        Packet<?> packet = ModLoader.get().createPacketToClient(payload);

        var vrPlayers = VisorServerImpl.INSTANCE.getPlayersWithVR();
        for (var players : getTrackedPlayers(packetOwner.getMcPlayer())) {
            boolean hasVisor = vrPlayers.containsKey(players.getPlayer().getUUID());
            boolean isPacketOwner = players.getPlayer() == packetOwner.getMcPlayer();
            if (!hasVisor
                    || isPacketOwner) {
                continue;
            }
            players.send(packet);
        }
        if(sendToOwner) {
            packetOwner.getMcPlayer().connection
                    .send(packet);
        }
    }


    public static Set<ServerPlayerConnection> getTrackedPlayers(ServerPlayer player) {
        ChunkMap chunkMap = player.serverLevel().getChunkSource().chunkMap;
        TrackedEntityAccessor entityAccessor = ((ChunkMapAccessor) chunkMap).getTrackedEntities().get(player.getId());
        return entityAccessor != null
                ? Collections.unmodifiableSet(entityAccessor.getPlayersTracking())
                : Collections.emptySet();
    }


}

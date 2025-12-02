package me.phoenixra.visor.core.server.network;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toclient.vrstate.VROtherStatePayloadToClient;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import me.phoenixra.visor.core.server.player.VRServerPlayerImpl;
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
import java.util.stream.Collectors;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ServerNetworking {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static void sendVRPacketTo(VRServerPlayer vrPlayer,
                                      VisorPayloadToClient payload) {
        if (MC.getConnection() == null) return;
        vrPlayer.getMcPlayer().connection
                .send(createVRPacket(payload));
    }

    public static Packet<?> createVRPacket(VisorPayloadToClient payload) {
        return ModLoader.get()
                .createPacketToClient(payload);
    }


    public static void kickDelayedIfNoVR(ServerPlayer serverPlayer) {
        scheduler.schedule(() -> {
            if(serverPlayer.server.isShutdown()){
                return;
            }
            if(serverPlayer.hasDisconnected()){
                return;
            }
            VRServerPlayer vrPlayer = VisorAPI.server()
                    .getVrPlayer(serverPlayer);

            if(serverPlayer.server.getPlayerList()
                    .isOp(serverPlayer.getGameProfile())){
                return;
            }

            if (vrPlayer == null || !vrPlayer.isVRActive()) {
                serverPlayer.connection.disconnect(
                        Component.literal(
                                "Server For VR player only!"
                        )
                );
            }

        }, 5, TimeUnit.SECONDS);
    }


    public static void updateStandingPose(ServerPlayer serverPlayer) {
        VRServerPlayer vrPlayer = VisorAPI.server()
                .getVrPlayer(serverPlayer);

        if (vrPlayer != null && vrPlayer.isVRActive()
                && vrPlayer.isCrawling()) {
            serverPlayer.setPose(Pose.SWIMMING);
        }
    }




    public static void sendPacketVRStateOf(ServerPlayer serverPlayer) {
        Map<UUID, VRServerPlayer> playersWithVR = VisorServerImpl.INSTANCE.getPlayersWithVR();
        VRServerPlayerImpl vrPlayer = (VRServerPlayerImpl) playersWithVR.get(serverPlayer.getUUID());
        if (vrPlayer == null) {
            return;
        }
        if (serverPlayer.hasDisconnected()) {
            playersWithVR.remove(serverPlayer.getUUID());
        }
        if (!vrPlayer.isVRActive()
                || vrPlayer.getPoseData().getBuffer() == null) {
            return;
        }
        sendPacketToTrackedVRPlayers(
                serverPlayer,
                new VROtherStatePayloadToClient(
                        serverPlayer.getUUID(),
                        vrPlayer.getPoseData().getBuffer(),
                        vrPlayer.getWorldScale(),
                        vrPlayer.getFullHeight()
                )
        );
    }



    public static void sendPacketToTrackedVRPlayers(ServerPlayer trackedBy,
                                                    VisorPayloadToClient payload) {
        Packet<?> packet = ModLoader.get().createPacketToClient(payload);

        for (var players : getTrackedVRPlayers(trackedBy)) {
            if (players.getPlayer() == trackedBy) {
                continue;
            }
            players.send(packet);
        }
    }


    public static Set<ServerPlayerConnection> getTrackedVRPlayers(ServerPlayer trackedBy) {
        ChunkMap chunkMap = trackedBy.serverLevel().getChunkSource().chunkMap;
        var vrPlayers = VisorServerImpl.INSTANCE.getPlayersWithVR();

        TrackedEntityAccessor entityAccessor = ((ChunkMapAccessor) chunkMap).getTrackedEntities()
                .get(trackedBy.getId());
        if(entityAccessor == null){
            return Collections.emptySet();
        }
        return entityAccessor.getPlayersTracking().stream()
                .filter(it->
                        vrPlayers.containsKey(it.getPlayer().getUUID())
                )
                .collect(Collectors.toSet());
    }


}

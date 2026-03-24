package org.vmstudio.visor.core.server.network;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.*;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;
import org.vmstudio.visor.core.server.VisorServerImpl;
import org.vmstudio.visor.mixin.common.accessors.ChunkMapAccessor;
import org.vmstudio.visor.mixin.common.accessors.TrackedEntityAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

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



    public static void sendVRStatePacketOf(ServerPlayer serverPlayer) {
        Map<UUID, VRServerPlayer> playersWithVR = VisorServerImpl.INSTANCE.getPlayersWithVR();
        VRServerPlayerImpl vrPlayer = (VRServerPlayerImpl) playersWithVR.get(serverPlayer.getUUID());
        if (vrPlayer == null) {
            return;
        }
        if (serverPlayer.hasDisconnected()) {
            playersWithVR.remove(serverPlayer.getUUID());
        }
        if (!vrPlayer.isVRActive()
                || vrPlayer.getPoseDataBuffer() == null) {
            return;
        }
        sendPacketToTrackedVRPlayers(
                serverPlayer,
                new VROtherPoseDataPayloadToClient(
                        serverPlayer.getUUID(),
                        vrPlayer.getPoseDataBuffer(),
                        vrPlayer.getWorldScale(),
                        vrPlayer.getFullHeight()
                )
        );


        boolean leftHanded = vrPlayer.isLeftHanded();
        boolean leftHandedLastSent = vrPlayer.isLeftHandedLastSent();
        if(leftHanded != leftHandedLastSent){
            sendPacketToTrackedVRPlayers(
                    serverPlayer,
                    new VROtherLeftHandedPayloadToClient(
                            serverPlayer.getUUID(),
                            leftHanded
                    )
            );
            vrPlayer.setLeftHandedLastSent(leftHanded);
        }

        String vrBody = vrPlayer.getVrBodyType();
        String vrBodyLastSent = vrPlayer.getVrBodyLastSent();
        if(!vrBody.equals(vrBodyLastSent)){
            sendPacketToTrackedVRPlayers(
                    serverPlayer,
                    new VROtherBodyTypePayloadToClient(
                            serverPlayer.getUUID(),
                            vrBody
                    )
            );
            vrPlayer.setVrBodyLastSent(vrBody);
        }

        var worldScale = vrPlayer.getWorldScale();
        var worldScaleLastSent = vrPlayer.getWorldScaleLastSent();
        if(worldScale != worldScaleLastSent){
            sendPacketToTrackedVRPlayers(
                    serverPlayer,
                    new VROtherWorldScalePayloadToClient(
                            serverPlayer.getUUID(),
                            worldScale
                    )
            );
            vrPlayer.setWorldScaleLastSent(worldScale);
        }

        var fullHeight = vrPlayer.getFullHeight();
        var fullHeightLastSent = vrPlayer.getFullHeightLastSent();
        if(fullHeight != fullHeightLastSent){
            sendPacketToTrackedVRPlayers(
                    serverPlayer,
                    new VROtherFullHeightPayloadToClient(
                            serverPlayer.getUUID(),
                            fullHeight
                    )
            );
            vrPlayer.setFullHeightLastSent(fullHeight);
        }
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

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

import java.util.*;
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

            if (vrPlayer == null) {
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
            VisorServerImpl.INSTANCE.removePlayer(serverPlayer);
        }
        if (vrPlayer.getPoseDataBuffer() == null) {
            return;
        }

        // ----- Compute trackers -----
        Set<ServerPlayerConnection> trackerConnections = getTrackedVRPlayers(serverPlayer);
        Set<UUID> currentTrackers = trackerConnections.stream()
                .map(c -> c.getPlayer().getUUID())
                .collect(Collectors.toSet());
        Set<UUID> newTrackers = new HashSet<>(currentTrackers);
        newTrackers.removeAll(vrPlayer.getKnownTrackers());
        vrPlayer.getKnownTrackers().retainAll(currentTrackers);
        vrPlayer.getKnownTrackers().addAll(currentTrackers);


        UUID uuid = serverPlayer.getUUID();
        String vrBody = vrPlayer.getVrBodyType();
        boolean leftHanded = vrPlayer.isLeftHanded();
        var worldScale = vrPlayer.getWorldScale();
        var fullHeight = vrPlayer.getFullHeight();
        var gunAngle = vrPlayer.getGunAngle();

        // ----- Send initial data to new trackers -----
        if (!newTrackers.isEmpty()) {
            for (ServerPlayerConnection trackerConnection : trackerConnections) {
                if (!newTrackers.contains(trackerConnection.getPlayer().getUUID())) {
                    continue;
                }
                if (trackerConnection.getPlayer() == serverPlayer) {
                    continue;
                }
                trackerConnection.send(createVRPacket(new VROtherBodyTypePayloadToClient(uuid, vrBody)));
                trackerConnection.send(createVRPacket(new VROtherLeftHandedPayloadToClient(uuid, leftHanded)));
                trackerConnection.send(createVRPacket(new VROtherWorldScalePayloadToClient(uuid, worldScale)));
                trackerConnection.send(createVRPacket(new VROtherFullHeightPayloadToClient(uuid, fullHeight)));
                trackerConnection.send(createVRPacket(new VROtherGunAnglePayloadToClient(uuid, gunAngle)));
            }
        }

        // Pose data
        sendPacketToConnections(
                serverPlayer, trackerConnections,
                false, null,
                new VROtherPoseDataPayloadToClient(uuid, vrPlayer.getPoseDataBuffer(), worldScale, fullHeight)
        );

        // ----- Send updated data to old trackers -----

        if (!vrBody.equals(vrPlayer.getVrBodyLastSent())) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, newTrackers,
                    new VROtherBodyTypePayloadToClient(uuid, vrBody)
            );
            vrPlayer.setVrBodyLastSent(vrBody);
        }

        if (leftHanded != vrPlayer.isLeftHandedLastSent()) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, newTrackers,
                    new VROtherLeftHandedPayloadToClient(uuid, leftHanded)
            );
            vrPlayer.setLeftHandedLastSent(leftHanded);
        }

        if (worldScale != vrPlayer.getWorldScaleLastSent()) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, newTrackers,
                    new VROtherWorldScalePayloadToClient(uuid, worldScale)
            );
            vrPlayer.setWorldScaleLastSent(worldScale);
        }

        if (fullHeight != vrPlayer.getFullHeightLastSent()) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, newTrackers,
                    new VROtherFullHeightPayloadToClient(uuid, fullHeight)
            );
            vrPlayer.setFullHeightLastSent(fullHeight);
        }

        // gunAngle is NOT part of the new-tracker burst, so don't exclude
        if (gunAngle != vrPlayer.getGunAngleLastSent()) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, null,
                    new VROtherGunAnglePayloadToClient(uuid, gunAngle)
            );
            vrPlayer.setGunAngleLastSent(gunAngle);
        }
    }


    private static void sendPacketToConnections(ServerPlayer tracked,
                                                Collection<ServerPlayerConnection> connections,
                                                boolean sendSelf,
                                                Set<UUID> excludeUuids,
                                                VisorPayloadToClient payload) {
        Packet<?> packet = ModLoader.get().createPacketToClient(payload);

        boolean wasSentSelf = false;
        for (var pc : connections) {
            ServerPlayer player = pc.getPlayer();
            if (player == tracked && !sendSelf) {
                wasSentSelf = true;
                continue;
            }
            if (excludeUuids != null && excludeUuids.contains(player.getUUID())) {
                continue;
            }
            pc.send(packet);
        }
        if (!wasSentSelf && sendSelf) {
            tracked.connection.send(packet);
        }
    }

    public static void sendPacketToTrackedVRPlayers(ServerPlayer tracked,
                                                    boolean sendSelf,
                                                    VisorPayloadToClient payload) {
        Packet<?> packet = ModLoader.get().createPacketToClient(payload);

        boolean wasSentSelf = false;
        for (var playerConnection : getTrackedVRPlayers(tracked)) {
            if (playerConnection.getPlayer() == tracked && !sendSelf) {
                wasSentSelf = true;
                continue;
            }
            playerConnection.send(packet);
        }
        if(!wasSentSelf && sendSelf){
            tracked.connection.send(packet);
        }
    }


    public static Set<ServerPlayerConnection> getTrackedVRPlayers(ServerPlayer trackedBy) {
        ChunkMap chunkMap = trackedBy.serverLevel().getChunkSource().chunkMap;
        var packetReceivers = VisorServerImpl.INSTANCE.getVisorPacketReceivers();

        TrackedEntityAccessor entityAccessor = ((ChunkMapAccessor) chunkMap).getTrackedEntities()
                .get(trackedBy.getId());
        if(entityAccessor == null){
            return Collections.emptySet();
        }
        return entityAccessor.getPlayersTracking().stream()
                .filter(it->
                        packetReceivers.containsKey(it.getPlayer().getUUID())
                )
                .collect(Collectors.toSet());
    }


}

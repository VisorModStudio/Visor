package org.vmstudio.visor.core.server.network;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorNetwork;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.*;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.compatibility.flashback.FlashbackCompatHelper;
import org.vmstudio.visor.compatibility.replaymod.ReplayCompatHelper;
import org.vmstudio.visor.core.common.addon.CoreAddonServer;
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

public class ServerNetworking {
    public static VisorChannel DEDICATED_CHANNEL;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static void createDedicatedChannel(@NotNull CoreAddonServer coreAddon){
        DEDICATED_CHANNEL = VisorChannel.builder(
                coreAddon,
                VisorNetwork.CORE_CHANNEL_ID,
                VisorNetwork.CORE_NETWORK_VERSION
        ).toServer(
                (id, buffer)->{
                    VisorCorePayloadID payloadId = VisorCorePayloadID.values()[id];
                    return VisorCorePayloadID.readToServer(payloadId, buffer);
                },
                ServerPacketHandler::handlePacket
        ).build();
        VisorNetwork.registerChannel(
                DEDICATED_CHANNEL
        );
    }

    public static void sendVRPacketTo(VRServerPlayer vrPlayer,
                                      VisorPayloadToClient payload) {
        vrPlayer.getMcPlayer().connection
                .send(createVRPacket(payload));
    }

    public static Packet<?> createVRPacket(VisorPayloadToClient payload) {
        return ModLoader.get()
                .createPacketToClient(VisorNetwork.CORE_CHANNEL_ID, payload);
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
                    .getVRPlayer(serverPlayer);

            if(serverPlayer.server.getPlayerList()
                    .isOp(serverPlayer.getGameProfile())){
                return;
            }

            if (vrPlayer == null) {
                serverPlayer.connection.disconnect(
                        Component.translatable("visor.messages.server_vr_only")
                );
            }

        }, 5, TimeUnit.SECONDS);
    }



    public static void sendVRStatePacketOf(ServerPlayer serverPlayer) {
        VRServerPlayerImpl vrPlayer = (VRServerPlayerImpl) VisorServerImpl.INSTANCE.getVRPlayer(serverPlayer);
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
        boolean overlayFocused = vrPlayer.isOverlayFocused();

        // Pose data
        sendPacketToConnections(
                serverPlayer, trackerConnections,
                false, null,
                new VROtherPoseDataPayloadToClient(uuid, vrPlayer.getPoseDataBuffer())
        );

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
                trackerConnection.send(createVRPacket(new VROtherOverlayFocusedPayloadToClient(uuid, overlayFocused)));
            }
        }

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

        if (overlayFocused != vrPlayer.isOverlayFocusedLastSent()) {
            sendPacketToConnections(
                    serverPlayer, trackerConnections,
                    false, newTrackers,
                    new VROtherOverlayFocusedPayloadToClient(uuid, overlayFocused)
            );
            vrPlayer.setOverlayFocusedLastSent(overlayFocused);
        }
    }


    private static void sendPacketToConnections(ServerPlayer tracked,
                                                Collection<ServerPlayerConnection> connections,
                                                boolean sendSelf,
                                                Set<UUID> excludeUuids,
                                                VisorPayloadToClient payload) {
        Packet<?> packet = ModLoader.get().createPacketToClient(VisorNetwork.CORE_CHANNEL_ID, payload);

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
        Packet<?> packet = ModLoader.get().createPacketToClient(VisorNetwork.CORE_CHANNEL_ID, payload);

        boolean wasSentSelf = false;
        boolean isRecordingModLoaded = ReplayCompatHelper.isLoaded()
                || FlashbackCompatHelper.isLoaded();
        for (var playerConnection : getTrackedVRPlayers(tracked)) {
            if (playerConnection.getPlayer() == tracked && !sendSelf && !isRecordingModLoaded) {
                wasSentSelf = true;
                continue;
            }
            playerConnection.send(packet);
        }
        if(!wasSentSelf && (sendSelf || isRecordingModLoaded)){
            tracked.connection.send(packet);
        }
    }


    public static Set<ServerPlayerConnection> getTrackedVRPlayers(ServerPlayer trackedBy) {
        ChunkMap chunkMap = trackedBy.serverLevel().getChunkSource().chunkMap;
        var vrServer = VisorServerImpl.INSTANCE;

        TrackedEntityAccessor entityAccessor = ((ChunkMapAccessor) chunkMap).getTrackedEntities()
                .get(trackedBy.getId());
        if(entityAccessor == null){
            return Collections.emptySet();
        }
        return entityAccessor.getPlayersTracking().stream()
                .filter(it->
                        vrServer.getVisorPlayer(it.getPlayer().getUUID()) != null
                )
                .collect(Collectors.toSet());
    }


}

package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toclient.vrstate.other.VROtherStartTrackingPayloadToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.level.Level;
import org.vmstudio.visor.api.common.network.buffer.PoseElementBuffer;
import org.vmstudio.visor.api.common.network.buffer.PoseTrackersBuffer;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.RecorderModHelper;
import org.vmstudio.visor.compatibility.flashback.FlashbackCompatHelper;
import org.vmstudio.visor.compatibility.replaymod.ReplayCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.network.ClientNetworking;

import java.util.*;
import java.util.function.Consumer;

public class VRClientPlayers {

    private static final Map<UUID, VRRemotePlayerImpl> remotePlayers = new HashMap<>();
    private static final Map<UUID,VRRemotePlayerImpl> remotePlayersReceived = Collections.synchronizedMap(new HashMap<>());
    private static final Map<UUID, PendingStartTracking> pendingStartTracking = Collections.synchronizedMap(new HashMap<>());
    private record PendingStartTracking(VROtherStartTrackingPayloadToClient payload, long expireTick) {}


    public static void onGameLoopStart() {
        ClientContext.localPlayer.onGameLoopStart();
    }

    public static void tick() {

        Level level = Minecraft.getInstance().level;

        if(level == null) return;

        tickPendingStartTracking();

        remotePlayers.putAll(remotePlayersReceived);

        remotePlayersReceived.clear();

        ClientNetworking.tickRecording();

        boolean inReplay = FlashbackCompatHelper.isInReplay();
        remotePlayers.values().removeIf(vrPlayer -> {
            var uuid = vrPlayer.getMcPlayer().getUUID();
            if(level.getPlayerByUUID(uuid) != null) return false;
            if(inReplay){
                pendingStartTracking.putIfAbsent(uuid, new PendingStartTracking(RecorderModHelper.startTrackingOf(vrPlayer), Long.MAX_VALUE));
            }
            return true;
        });
    }


    public static void preTick() {
        ClientContext.localPlayer.preTick();
        preTickRemote();
    }

    public static void postTick(){
        ClientContext.localPlayer.postTick();
        postTickRemote();
    }

    public static void preRender(float partialTicks){
        ClientContext.localPlayer.preRender(partialTicks);
        remotePlayers.values().forEach(it->{
            it.preRender(partialTicks);
        });
    }

    // flashback's export calls these too and this keeps pre/post in pairs
    private static boolean remoteTickOpen;

    public static void preTickRemote() {
        if(remoteTickOpen) return;
        remoteTickOpen = true;
        remotePlayers.values().forEach(VRRemotePlayerImpl::preTick);
    }

    public static void postTickRemote() {
        if(!remoteTickOpen) return;
        remoteTickOpen = false;
        remotePlayers.values().forEach(VRRemotePlayerImpl::postTick);
    }

    public static void preRenderRemote(float partialTicks){
        remotePlayers.values().forEach(it -> it.preRender(partialTicks));
    }


    // start tracking before the player spawned, wait 100 ticks live, no limit in a replay (костыльность...)
    public static void deferStartTracking(VROtherStartTrackingPayloadToClient payload){
        Level level = Minecraft.getInstance().level;
        if(level == null || level.getPlayerByUUID(payload.playerUUID()) != null) return;
        long expireTick = FlashbackCompatHelper.isInReplay() ? Long.MAX_VALUE : VisorState.TICK_COUNT + 100;
        pendingStartTracking.put(payload.playerUUID(), new PendingStartTracking(payload, expireTick));
    }

    private static void tickPendingStartTracking(){
        if(pendingStartTracking.isEmpty()) return;
        synchronized (pendingStartTracking) {
            pendingStartTracking.values().removeIf(pending -> {
                var payload = pending.payload();
                var mcPlayer = getValidPacketReceiverMc(payload.playerUUID());
                if(mcPlayer == null) return VisorState.TICK_COUNT > pending.expireTick();
                var receiver = getPacketReceiver(payload.playerUUID());
                if(receiver == null){
                    receivedNewPlayer(new VRRemotePlayerImpl(mcPlayer, payload));
                }else{
                    receiver.receivedStartTracking(mcPlayer, payload);
                }
                return true;
            });
        }
    }

    public static RemotePlayer getValidPacketReceiverMc(UUID uuid){

        if(ClientContext.localPlayer.getMcPlayer() != null
                && ClientContext.localPlayer.getMcPlayer().getUUID().equals(uuid)){
            return null;
        }
        Level level = Minecraft.getInstance().level;

        if(level == null) return null;
        var player = level.getPlayerByUUID(uuid);
        if(!(player instanceof RemotePlayer remotePlayer)){
            return null;
        }

        return remotePlayer;
    }
    public static void handleOnValidReceiver(UUID uuid, Consumer<VRRemotePlayerImpl> task) {
        var receiver = getPacketReceiver(uuid);
        if(receiver != null){
            task.accept(receiver);
        }
    }
    public static VRClientPlayer getPlayer(UUID uuid) {
        if(ClientContext.localPlayer.getMcPlayer() != null
                && ClientContext.localPlayer.getMcPlayer().getUUID().equals(uuid)){
            if (VisorState.get().isActive()) {
                return ClientContext.localPlayer;
            }
            return null;
        }
        return remotePlayers.get(uuid);
    }
    public static VRRemotePlayerImpl getPacketReceiver(UUID uuid) {
        var receiver = remotePlayers.get(uuid);
        if(receiver == null){
            receiver = remotePlayersReceived.get(uuid);
        }
        return receiver;
    }
    public static VRClientPlayer getPlayer(Entity entity) {
        return getPlayer(entity.getUUID());
    }
    public static boolean isTracked(UUID uuid) {
        return getPlayer(uuid) != null;
    }
    public static boolean isTracked(Entity entity) {
        return getPlayer(entity.getUUID()) != null;
    }

    public static void removePlayer(UUID uuid) {
        pendingStartTracking.remove(uuid);
        remotePlayers.remove(uuid);
        remotePlayersReceived.remove(uuid);
    }
    public static void receivedNewPlayer(VRRemotePlayerImpl vrPlayer){
        remotePlayersReceived.put(vrPlayer.getMcPlayer().getUUID(), vrPlayer);
    }

    public static Collection<VRRemotePlayerImpl> getRemotePlayers(){
        return remotePlayers.values();
    }


    public static void dispose() {
        remoteTickOpen = false;
        remotePlayers.clear();
        remotePlayersReceived.clear();
        pendingStartTracking.clear();
    }

}

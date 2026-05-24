package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.level.Level;
import org.vmstudio.visor.api.common.network.buffer.PoseElementBuffer;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.flashback.FlashbackCompatHelper;
import org.vmstudio.visor.compatibility.replaymod.ReplayCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;

import java.util.*;

public class VRClientPlayers {

    private static final Map<UUID, VRRemotePlayerImpl> remotePlayers = new HashMap<>();
    private static final Map<UUID,VRRemotePlayerImpl> remotePlayersReceived = Collections.synchronizedMap(new HashMap<>());


    public static void onGameLoopStart() {
        ClientContext.localPlayer.onGameLoopStart();
    }

    public static void tick() {

        Level level = Minecraft.getInstance().level;

        if(level == null) return;

        remotePlayers.putAll(remotePlayersReceived);

        remotePlayersReceived.clear();

        remotePlayers.keySet().removeIf(
                uuid -> level.getPlayerByUUID(uuid) == null
        );
    }


    public static void preTick() {
        ClientContext.localPlayer.preTick();
        remotePlayers.values().forEach(VRRemotePlayerImpl::preTick);
    }

    public static void postTick(){
        ClientContext.localPlayer.postTick();
        remotePlayers.values().forEach(VRRemotePlayerImpl::postTick);
    }

    public static void preRender(float partialTicks){
        ClientContext.localPlayer.preRender(partialTicks);
        remotePlayers.values().forEach(it->{
            it.preRender(partialTicks);
        });
    }

    public static void preTickRemote() {
        remotePlayers.values().forEach(VRRemotePlayerImpl::preTick);
    }

    public static void postTickRemote() {
        remotePlayers.values().forEach(VRRemotePlayerImpl::postTick);
    }

    public static void preRenderRemote(float partialTicks){
        remotePlayers.values().forEach(it -> it.preRender(partialTicks));
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
    public static VRRemotePlayerImpl ensurePacketReceiver(UUID uuid, RemotePlayer mcPlayer) {
        var receiver = getPacketReceiver(uuid);
        if(receiver != null){
            return receiver;
        }
        PoseElementBuffer empty = new PoseElementBuffer(new Vector3f(), new Quaternionf());
        receiver = new VRRemotePlayerImpl(
                mcPlayer,
                new PoseDataBuffer(empty, empty, empty)
        );
        receivedNewPlayer(receiver);
        return receiver;
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
        remotePlayers.clear();
        remotePlayersReceived.clear();
    }

}

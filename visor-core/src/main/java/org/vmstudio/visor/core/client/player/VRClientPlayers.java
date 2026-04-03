package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.level.Level;
import org.vmstudio.visor.core.client.VisorState;

import java.util.*;

public class VRClientPlayers {
    @Getter
    private static final VRLocalPlayerImpl localPlayer = new VRLocalPlayerImpl();

    private static final Map<UUID, VRRemotePlayerImpl> remotePlayers = new HashMap<>();
    private static final Map<UUID,VRRemotePlayerImpl> remotePlayersReceived = Collections.synchronizedMap(new HashMap<>());


    public static void onGameLoopStart() {
        localPlayer.onGameLoopStart();
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
        localPlayer.preTick();
        remotePlayers.values().forEach(VRRemotePlayerImpl::preTick);
    }

    public static void postTick(){
        localPlayer.postTick();
        remotePlayers.values().forEach(VRRemotePlayerImpl::postTick);
    }

    public static void preRender(float partialTicks){
        localPlayer.preRender(partialTicks);
        remotePlayers.values().forEach(it->{
            it.preRender(partialTicks);
        });
    }


    public static void handlePosePacket(UUID uuid,
                                        PoseDataBuffer poseBuffer) {
        var remotePlayer = getValidPacketReceiverMc(uuid);
        if(remotePlayer == null){
            return;
        }
        var vrPlayer = remotePlayers.get(uuid);

        if(vrPlayer == null){
            vrPlayer = new VRRemotePlayerImpl(
                    remotePlayer,
                    poseBuffer
            );
            receivedNewPlayer(vrPlayer);
        }else{
            vrPlayer.receivedPosePacked(
                    remotePlayer,
                    poseBuffer
            );
        }
    }



    public static RemotePlayer getValidPacketReceiverMc(UUID uuid){
        if(localPlayer.getMcPlayer() != null
                && localPlayer.getMcPlayer().getUUID().equals(uuid)){
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

    public static VRClientPlayer getPlayer(UUID uuid) {
        if(localPlayer.getMcPlayer() != null
                && localPlayer.getMcPlayer().getUUID().equals(uuid)){
            if (VisorState.get().isActive()) {
                return localPlayer;
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

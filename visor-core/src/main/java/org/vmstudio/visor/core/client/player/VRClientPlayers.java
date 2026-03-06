package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.level.Level;

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


    public static void receivedPacket(UUID uuid,
                                      PoseDataBuffer poseBuffer,
                                      float worldScale,
                                      float fullHeight) {
        if(localPlayer.getMcPlayer() != null
                && localPlayer.getMcPlayer().getUUID().equals(uuid)){
            return;
        }

        Level level = Minecraft.getInstance().level;

        if(level == null) return;
        var player = level.getPlayerByUUID(uuid);
        if(!(player instanceof RemotePlayer remotePlayer)){
            return;
        }

        var vrPlayer = remotePlayers.get(uuid);
        if(vrPlayer == null){
            vrPlayer = new VRRemotePlayerImpl(
                    remotePlayer,
                    poseBuffer,
                    worldScale,
                    fullHeight
            );
        }else{
            vrPlayer.receivedPacked(
                    remotePlayer,
                    poseBuffer,
                    worldScale,
                    fullHeight
            );
        }
        remotePlayersReceived.put(uuid, vrPlayer);
    }

    public static VRClientPlayer getPlayer(UUID uuid) {
        if(localPlayer.getMcPlayer() != null
                && localPlayer.getMcPlayer().getUUID().equals(uuid)){
            return localPlayer;
        }
        return remotePlayers.get(uuid);
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


    public static void dispose() {
        remotePlayers.clear();
        remotePlayersReceived.clear();
    }

}

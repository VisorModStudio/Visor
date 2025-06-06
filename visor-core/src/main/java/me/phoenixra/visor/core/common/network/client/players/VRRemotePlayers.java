package me.phoenixra.visor.core.common.network.client.players;

import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRRemotePlayers {
    private final Map<UUID, VRRemotePlayerData> vrPlayers = new HashMap<>();
    private final Map<UUID, VRRemotePlayerData> vrPlayersLastTick = new HashMap<>();
    private final Map<UUID, VRRemotePlayerData> vrPlayersReceived = Collections.synchronizedMap(new HashMap<>());

    private static VRRemotePlayers instance;



    public void tick() {
        this.vrPlayersLastTick.putAll(this.vrPlayers);

        this.vrPlayers.putAll(this.vrPlayersReceived);

        Level level = Minecraft.getInstance().level;

        if(level == null) return;

        Iterator<UUID> iterator = this.vrPlayers
                .keySet().iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (level.getPlayerByUUID(uuid) != null){
                return;
            }

            //if player entity not in a client world
            iterator.remove();
            this.vrPlayersLastTick.remove(uuid);
            this.vrPlayersReceived.remove(uuid);
        }
    }

    public VRRemotePlayerData getPlayer(UUID uuid) {

        VRRemotePlayerData playerData1 = this.vrPlayers.get(uuid);

        if (playerData1 != null
                && this.vrPlayersLastTick.containsKey(uuid)) {
            VRRemotePlayerData playerDataPrev = this.vrPlayersLastTick.get(uuid);

            //average between current and previous
            float frameTime = Minecraft.getInstance().getFrameTime();
            return new VRRemotePlayerData(
                    //offhand rotation
                    playerData1.offhandRotation(),
                    //offhand direction
                    VRMathUtils.lerpVector(
                            playerDataPrev.offhandDirection(),
                            VRMathUtils.convertToMcVector(
                                    playerData1.offhandRotation()
                                            .transform(VRMathUtils.forwardVector, new Vector3f())
                            ),
                            frameTime
                    ),
                    //offhand position
                    VRMathUtils.lerpVector(
                            playerDataPrev.offhandPosition(),
                            playerData1.offhandPosition(),
                            frameTime
                    ),
                    //mainHand rotation
                    playerData1.mainHandRotation(),
                    //mainHand direction
                    VRMathUtils.lerpVector(
                            playerDataPrev.mainHandDirection(),
                            VRMathUtils.convertToMcVector(
                                    playerData1.mainHandRotation()
                                            .transform(VRMathUtils.forwardVector, new Vector3f())
                            ),
                            frameTime
                    ),
                    //mainHand position
                    VRMathUtils.lerpVector(
                            playerDataPrev.mainHandPosition(),
                            playerData1.mainHandPosition(),
                            frameTime
                    ),
                    //HMD rotation
                    playerData1.hmdRotation(),
                    //HMD direction
                    VRMathUtils.lerpVector(
                            playerDataPrev.hmdDirection(),
                            VRMathUtils.convertToMcVector(
                                    playerData1.hmdRotation()
                                            .transform(VRMathUtils.forwardVector, new Vector3f())
                            ),
                            frameTime
                    ),
                    //HMD position
                    VRMathUtils.lerpVector(
                            playerDataPrev.hmdPosition(),
                            playerData1.hmdPosition(),
                            frameTime
                    ),
                    playerData1.worldScale(),
                    playerData1.heightScale(),
                    playerData1.headsetModel(),
                    playerData1.leftHanded()
            );
        } else {
            return playerData1;
        }
    }

    public void applyPlayer(UUID uuid,
                            PlayerPoseBuffer poseBuffer,
                            float worldScale,
                            float heightScale,
                            boolean localPlayer) {
        if(!localPlayer && MC.player.getUUID().equals(uuid)){
            return;
        }

        Vector3f hmdDir = poseBuffer.hmd()
                .orientation().transform(VRMathUtils.forwardVector, new Vector3f());
        Vector3f mainHandDir = poseBuffer.mainHand()
                .orientation().transform(VRMathUtils.forwardVector, new Vector3f());
        Vector3f offhandDir = poseBuffer.offhand()
                .orientation().transform(VRMathUtils.forwardVector, new Vector3f());
        //[0.5; 1.5] bounds
        heightScale = Math.max(
                0.5f,
                Math.min(
                        1.5f,
                        heightScale
                )
        );

        VRRemotePlayerData playerData = new VRRemotePlayerData(
                //OFFHAND
                poseBuffer.offhand().orientation(),
                new Vec3(
                        offhandDir.x(),
                        offhandDir.y(),
                        offhandDir.z()
                ),
                poseBuffer.offhand().position(),
                //MAIN HAND
                poseBuffer.mainHand().orientation(),
                new Vec3(
                        mainHandDir.x(),
                        mainHandDir.y(),
                        mainHandDir.z()
                ),
                poseBuffer.mainHand().position(),
                //HMD
                poseBuffer.hmd().orientation(),
                new Vec3(hmdDir.x(), hmdDir.y(), hmdDir.z()),
                poseBuffer.hmd().position(),
                //MISC
                worldScale,
                heightScale,
                0,
                poseBuffer.leftHanded()
        );

        this.vrPlayersReceived.put(uuid, playerData);
    }


    public void applyPlayer(UUID uuid,
                            PlayerPoseBuffer poseBuffer,
                            float worldScale,
                            float heightScale) {
        this.applyPlayer(uuid, poseBuffer, worldScale, heightScale, false);
    }


    public void removePlayer(UUID player) {
        this.vrPlayers.remove(player);
        this.vrPlayersLastTick.remove(player);
        this.vrPlayersReceived.remove(player);
    }

    public boolean isTracked(UUID uuid) {
        return this.vrPlayers.containsKey(uuid);
    }

    public static void clear() {
        if (instance != null) {
            instance.vrPlayers.clear();
            instance.vrPlayersLastTick.clear();
            instance.vrPlayersReceived.clear();
        }
    }

    public static VRRemotePlayers getInstance() {
        if (instance == null) {
            instance = new VRRemotePlayers();
        }

        return instance;
    }

}

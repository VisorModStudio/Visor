package me.phoenixra.visor.core.server;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.network.buffer.PoseDataBuffer;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import me.phoenixra.visor.core.common.data.PlayerPoseServerImpl;
import me.phoenixra.visor.modified.common.ServerPlayerModified;
import net.minecraft.server.level.ServerPlayer;


public class VRServerPlayerImpl implements VRServerPlayer {
    @Getter
    public ServerPlayer mcPlayer;

    @Getter
    private final PlayerPoseServerImpl poseData = new PlayerPoseServerImpl();

    @Setter
    private boolean vrActive = false;


    @Getter @Setter
    private float worldScale = 1.0F;
    @Getter @Setter
    private float height = 1.0F;
    @Getter
    private float rotationY;

    @Getter
    public float bowTension;

    @Getter
    private boolean leftHanded;

    @Getter
    public boolean crawling;

    public VRServerPlayerImpl(ServerPlayer player) {
        this.mcPlayer = player;
    }



    public void poseUpdateReceived(PoseDataBuffer buffer){
        poseData.update(
                buffer,
                mcPlayer.position().toVector3f(),
                worldScale
        );
        leftHanded = poseData.getBuffer().leftHanded();
    }


    public void updateRotationY(float rotationY){
        this.rotationY = rotationY;
        ((ServerPlayerModified)mcPlayer).visor$setRotationYCached(rotationY);
    }

    @Override
    public boolean isVRActive() {
        return vrActive;
    }
}

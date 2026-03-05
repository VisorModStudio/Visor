package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;


public class VRServerPlayerImpl implements VRServerPlayer {
    @Getter @Setter
    private ServerPlayer mcPlayer;


    @Getter
    private final PlayerPoseServerImpl poseDataPrev = new PlayerPoseServerImpl();

    @Getter
    private final PlayerPoseServerImpl poseDataRelative = new PlayerPoseServerImpl();

    @Getter
    private final PlayerPoseServerImpl poseData = new PlayerPoseServerImpl();

    @Getter
    private final PoseHistoryImpl poseHistoryRelative;
    @Getter
    private final PoseHistoryImpl poseHistoryTick;

    @Setter
    private boolean vrActive = false;


    @Getter @Setter
    private float worldScale = 1.0F;
    @Getter @Setter
    private float fullHeight = 1.0F;
    @Getter
    private float rotationY;

    @Getter @Setter
    private float bowTension;

    @Getter
    private boolean leftHanded;

    @Getter
    private boolean crawling;

    public VRServerPlayerImpl(ServerPlayer player) {
        this.mcPlayer = player;
        poseHistoryRelative = new PoseHistoryImpl(poseDataRelative);
        poseHistoryTick = new PoseHistoryImpl(poseData);
    }



    public void poseUpdateReceived(PoseDataBuffer buffer){
        poseDataPrev.copyFrom(poseData);

        poseDataRelative.update(
                buffer,
                VRMathUtils.ZERO_VECTOR
        );
        poseData.update(
                buffer,
                mcPlayer.position().toVector3f()
        );

        var historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(poseDataRelative);
        poseHistoryRelative.addEntry(historyEntry);

        historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(poseDataPrev);
        poseHistoryTick.addEntry(historyEntry);

        leftHanded = poseData.getBuffer().leftHanded();
    }

    public void setCrawling(boolean crawling) {
        this.crawling = crawling;
        if(crawling) {
            mcPlayer.setPose(Pose.SWIMMING);
        }
    }

    public void updateRotationY(float rotationY){
        this.rotationY = rotationY;
        ((ServerPlayerExtension)mcPlayer).visor$setRotationYCached(rotationY);
    }

    @Override
    public boolean isVRActive() {
        return vrActive;
    }
}

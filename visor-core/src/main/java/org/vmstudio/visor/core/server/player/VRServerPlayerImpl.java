package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toserver.vrstate.LeftHandedPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.vrstate.VRBodyTypePayloadToServer;
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
    private PoseDataBuffer poseDataBuffer;

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

    @Getter @Setter
    private String vrBodyType = "null";

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

    @Getter @Setter
    private boolean leftHanded;

    @Getter
    private boolean crawling;



    @Getter @Setter
    private boolean leftHandedLastSent = false;
    @Getter @Setter
    private String vrBodyLastSent = null;
    @Getter @Setter
    private float worldScaleLastSent = 1.0f;
    @Getter @Setter
    private float fullHeightLastSent = 1.0F;

    public VRServerPlayerImpl(ServerPlayer player) {
        this.mcPlayer = player;
        poseHistoryRelative = new PoseHistoryImpl(poseDataRelative);
        poseHistoryTick = new PoseHistoryImpl(poseData);
    }



    public void receivedPosePacket(PoseDataBuffer poseDataBuffer){
        poseDataPrev.copyFrom(poseData);

        this.poseDataBuffer = poseDataBuffer;

        poseDataRelative.update(
                poseDataBuffer,
                VRMathUtils.ZERO_VECTOR
        );
        poseData.update(
                poseDataBuffer,
                mcPlayer.position().toVector3f()
        );

        var historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(poseDataRelative);
        poseHistoryRelative.addEntry(historyEntry);

        historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(poseDataPrev);
        poseHistoryTick.addEntry(historyEntry);

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

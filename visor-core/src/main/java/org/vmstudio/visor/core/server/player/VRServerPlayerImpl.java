package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;


@Getter
public class VRServerPlayerImpl extends VisorPacketReceiver implements VRServerPlayer {



    private PoseDataBuffer poseDataBuffer;

    private final PlayerPoseServerImpl posePrevious = new PlayerPoseServerImpl();
    private final PlayerPoseServerImpl poseRelative = new PlayerPoseServerImpl();
    private final PlayerPoseServerImpl pose = new PlayerPoseServerImpl();


    private final PoseHistoryImpl poseHistoryRelative;
    private final PoseHistoryImpl poseHistoryTick;

    @Setter
    private String vrBodyType = "null";

    @Setter
    private boolean VRActive = false;

    @Setter
    private float worldScale = 1.0F;
    @Setter
    private float fullHeight = 1.0F;
    private float rotationY;

    @Setter
    private HandType activeHand;

    @Setter
    private boolean leftHanded;

    private boolean crawling;

    @Setter
    private int offhandSlot;


    @Setter
    private boolean leftHandedLastSent = false;
    @Setter
    private String vrBodyLastSent = null;
    @Setter
    private float worldScaleLastSent = 1.0f;
    @Setter
    private float fullHeightLastSent = 1.0F;

    public VRServerPlayerImpl(ServerPlayer player) {
        super(player);
        poseHistoryRelative = new PoseHistoryImpl(poseRelative);
        poseHistoryTick = new PoseHistoryImpl(pose);
    }



    public void receivedPosePacket(PoseDataBuffer poseDataBuffer){
        posePrevious.copyFrom(pose);

        this.poseDataBuffer = poseDataBuffer;

        poseRelative.update(
                poseDataBuffer,
                VRMathUtils.ZERO_VECTOR
        );
        pose.update(
                poseDataBuffer,
                mcPlayer.position().toVector3f()
        );

        var historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(poseRelative);
        poseHistoryRelative.addEntry(historyEntry);

        historyEntry = new PlayerPoseServerImpl();
        historyEntry.copyFrom(posePrevious);
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
}

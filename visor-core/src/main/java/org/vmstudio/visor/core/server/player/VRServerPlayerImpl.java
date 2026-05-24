package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Getter
public class VRServerPlayerImpl extends VisorServerPlayerImpl implements VRServerPlayer {



    private PoseDataBuffer poseDataBuffer;

    private final PlayerPoseServerImpl poseDataPrevious = new PlayerPoseServerImpl(this);
    private final PlayerPoseServerImpl poseDataRelative = new PlayerPoseServerImpl(this);
    private final PlayerPoseServerImpl poseData = new PlayerPoseServerImpl(this);


    private final PoseHistoryImpl poseHistoryRelative;
    private final PoseHistoryImpl poseHistoryTick;

    @Setter
    private String vrBodyType = "null";

    @Setter
    private float worldScale = 1.0F;
    @Setter
    private float fullHeight = VRPlayer.DEFAULT_FULL_HEIGHT;
    private float rotationY;

    @Setter
    private HandType activeHand = HandType.MAIN;

    @Setter
    private boolean leftHanded;

    private boolean crawling;

    private int offhandSlot;
    @Setter
    private float gunAngle = VRPlayer.DEFAULT_GUN_ANGLE;

    @Setter
    private boolean leftHandedLastSent = false;
    @Setter
    private String vrBodyLastSent = null;
    @Setter
    private float worldScaleLastSent = 1.0f;
    @Setter
    private float fullHeightLastSent = VRPlayer.DEFAULT_FULL_HEIGHT;
    @Setter
    private float gunAngleLastSent = VRPlayer.DEFAULT_GUN_ANGLE;
    @Setter
    private boolean overlayFocused;
    @Setter
    private boolean overlayFocusedLastSent;

    private final Set<UUID> knownTrackers = new HashSet<>();

    public VRServerPlayerImpl(ServerPlayer player) {
        super(player);
        poseHistoryRelative = new PoseHistoryImpl(poseDataRelative);
        poseHistoryTick = new PoseHistoryImpl(poseData);
    }



    public void receivedPosePacket(PoseDataBuffer poseDataBuffer){
        poseDataPrevious.copyFrom(poseData);

        this.poseDataBuffer = poseDataBuffer;

        poseDataRelative.update(
                poseDataBuffer,
                VRMathUtils.ZERO_VECTOR
        );
        poseData.update(
                poseDataBuffer,
                mcPlayer.position().toVector3f()
        );

        var historyEntry = new PlayerPoseServerImpl(this);
        historyEntry.copyFrom(poseDataRelative);
        poseHistoryRelative.addEntry(historyEntry);

        historyEntry = new PlayerPoseServerImpl(this);
        historyEntry.copyFrom(poseDataPrevious);
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
    public void updateOffhandSlot(int slot){
        this.offhandSlot = slot;
        ((ServerPlayerExtension)mcPlayer).visor$setOffhandSlotCached(slot);
    }
}

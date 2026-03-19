package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;

@Getter
public abstract class VRBodyPart {

    public static VRBodyPart SIMPLE_MAIN_HAND = new VRBodyPart("main_hand") {
        @Override
        public void update(VRPlayerPoseClient poseClient) {
            this.pose.copyFrom(poseClient.getMainHand());
        }
    };

    public static VRBodyPart SIMPLE_OFFHAND = new VRBodyPart("offhand") {
        @Override
        public void update(VRPlayerPoseClient poseClient) {
            this.pose.copyFrom(poseClient.getOffhand());
        }
    };

    private final String id;
    protected final VRPose pose;


    public VRBodyPart(@NotNull String id){
        this.id = id;
        this.pose = VRPose.create();

    }

    public abstract void update(VRPlayerPoseClient poseClient);

}

package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
public abstract class VRBodyPart {
    public static final String ID_HEAD = "head";
    public static final String ID_MAIN_HAND = "main_hand";
    public static final String ID_OFFHAND = "offhand";


    private final String id;
    protected final VRPose pose;


    public VRBodyPart(@NotNull String id){
        this.id = id;
        this.pose = VRPose.create();

    }

    public abstract void update(VRPlayerPoseClient poseClient);

    public void copyFrom(@NotNull VRBodyPart other){
        pose.copyFrom(other.pose);
    }


    public static VRBodyPart createSimpleHead() {
        return new VRBodyPart(ID_HEAD) {
            @Override
            public void update(VRPlayerPoseClient poseClient) {
                this.pose.copyFrom(poseClient.getHmd());
            }
        };
    }

    public static VRBodyPart createSimpleMainHand() {
        return new VRBodyPart(ID_MAIN_HAND) {
            @Override
            public void update(VRPlayerPoseClient poseClient) {
                this.pose.copyFrom(poseClient.getMainHand());
            }
        };
    }

    public static VRBodyPart createSimpleOffhand() {
        return new VRBodyPart(ID_OFFHAND) {
            @Override
            public void update(VRPlayerPoseClient poseClient) {
                this.pose.copyFrom(poseClient.getOffhand());
            }
        };
    }
}

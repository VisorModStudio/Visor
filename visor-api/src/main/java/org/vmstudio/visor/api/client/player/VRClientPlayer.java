package org.vmstudio.visor.api.client.player;

import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * The common interface for VR client players, both local and remote
 */
public interface VRClientPlayer extends VRPlayer {



    /**
     * Get VR body
     *
     * @return the VR body
     */
    VRBodyType getBodyType();

    /**
     * Get pose data of specified type
     *
     * @param type the pose data type
     *
     * @return pose data
     */
    @NotNull
    VRPlayerPoseClient getPoseData(@NotNull PlayerPoseType type);

    /**
     * Scale of the player model relative to vanilla
     *
     * @param poseType the pose to get world scale from
     *
     * @return model scale
     */
    float getModelScale(@NotNull PlayerPoseType poseType);

    default float getModelScale() {
        return getModelScale(PlayerPoseType.RENDER);
    }

    @Override
    default @NotNull VRPlayerPoseClient getPoseDataPrevious() {
        return getPoseData(PlayerPoseType.PREV_TICK);
    }
    @Override
    default @NotNull VRPlayerPoseClient getPoseDataRoom() {
        return getPoseData(PlayerPoseType.ROOM);
    }
    @Override
    default @NotNull VRPlayerPoseClient getPoseData() {
        return getPoseData(PlayerPoseType.TICK);
    }



}

package org.vmstudio.visor.api.client.player;

import net.minecraft.client.player.AbstractClientPlayer;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPoseHistory;
import org.jetbrains.annotations.NotNull;

/**
 * The common interface for VR client players, both local and remote
 */
public interface VRClientPlayer extends VRPlayer {

    /**
     * Get local player associated with this instance
     *
     * @return mc player
     */
    AbstractClientPlayer getMcPlayer();

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
    VRPlayerPoseClient getPose(@NotNull PlayerPoseType type);

    @Override
    default @NotNull VRPlayerPoseClient getPosePrevious() {
        return getPose(PlayerPoseType.PREV_TICK);
    }
    @Override
    default @NotNull VRPlayerPoseClient getPoseRelative() {
        return getPose(PlayerPoseType.RELATIVE);
    }
    @Override
    default @NotNull VRPlayerPoseClient getPose() {
        return getPose(PlayerPoseType.TICK);
    }



}

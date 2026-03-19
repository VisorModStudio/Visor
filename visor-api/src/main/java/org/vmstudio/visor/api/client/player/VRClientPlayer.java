package org.vmstudio.visor.api.client.player;

import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.player.VRPoseHistory;
import org.jetbrains.annotations.NotNull;

/**
 * The common class for VR client players, both local and remote
 */
public interface VRClientPlayer {
    /**
     * Get VR body
     *
     * @return the VR body
     */
    VRBody getBody();

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
     * get pose history for relative type
     *
     * @return pose history
     */
    @NotNull
    VRPoseHistory getPoseHistoryRelative();

    /**
     * get pose history for tick type
     *
     * @return pose history
     */
    @NotNull
    VRPoseHistory getPoseHistoryTick();


    /**
     * Get full height
     *
     * @return full height
     */
    float getFullHeight();

    /**
     * Get actual height
     *
     * @return actual height
     */
    default float getActualHeight(){
        return getPoseData(PlayerPoseType.RELATIVE).getHeadPivot().y();
    }

    /**
     * Get full height scale.
     * <p>
     *     It is the ratio between {@link #getFullHeight()}
     *     and height of a minecraft player
     * </p>
     *
     * @return full height scale
     */
    default float getFullHeightScale() {

        return getFullHeight() / 1.52f;
    }


    /**
     * If this VR player is left-handed
     *
     * @return true/false
     */
    boolean isLeftHanded();
}

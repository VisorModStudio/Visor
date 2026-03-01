package org.vmstudio.visor.api.client.player.pose;

import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.VRRemotePlayer;

/**
 * Describes which stage of the player pose is being used.
 * <p>
 * Each value represents a different step in the client game loop.
 * </p>
 */
public enum PlayerPoseType {

    /**
     * Pose that is not converted to world coordinates yet.
     * <p>
     *   For {@link VRLocalPlayer} it is relative to VR room
     *   and updated each frame at the start of a game loop
     * </p>
     * <p>
     *     For {@link VRRemotePlayer} it is relative to player
     *     and updated from server
     * </p>
     */
    RELATIVE,

    /**
     * Pose from the previous game tick.
     */
    PREV_TICK,

    /**
     * Pose for the current game tick.
     */
    TICK,

    /**
     * Pose used for rendering.
     * <p>
     *   Derived from interpolation between PREV_TICK and TICK
     *   to provide smooth visuals.
     * </p>
     */
    RENDER

}

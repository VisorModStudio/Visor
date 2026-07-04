package org.vmstudio.visor.api.client.player.pose;


/**
 * Describes which stage of the player pose is being used.
 * <p>
 * Each value represents a different step in the client game loop.
 * </p>
 */
public enum PlayerPoseType {

    /**
     * Pose relative to VR room coordinate system.
     */
    ROOM,

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

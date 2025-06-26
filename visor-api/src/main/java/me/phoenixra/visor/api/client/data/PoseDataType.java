package me.phoenixra.visor.api.client.data;

public enum PoseDataType {

    /**
     * Pose is relative to VR room
     */
    ROOM,

    /**
     * Pose is relative to world pre-ticking
     */
    PRE_TICK,

    /**
     * Pose is relative to world post-ticking
     */
    POST_TICK,

    /**
     * Pose is relative to world rendering
     */
    RENDER


}

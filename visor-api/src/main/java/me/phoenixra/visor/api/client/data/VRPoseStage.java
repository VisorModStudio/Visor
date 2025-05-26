package me.phoenixra.visor.api.client.data;

public enum VRPoseStage {

    /**
     * Pose from this stage is relative to VR room
     */
    ROOM,

    /**
     * Pose from this stage is relative to world pre-ticking
     */
    PRE_TICK,

    /**
     * Pose from this stage is relative to world post-ticking
     */
    POST_TICK,

    /**
     * Pose from this stage is relative to world rendering
     */
    RENDER


}

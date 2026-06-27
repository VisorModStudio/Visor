package org.vmstudio.visor.api.common.player;

public enum VRBodyPartType {
    /**
     * HMD
     */
    HEAD,
    /**
     * Controller Main ( leftHanded false = RIGHT, true = LEFT )
     */
    MAIN_HAND,
    /**
     * Controller Offhand ( leftHanded false = LEFT, true = RIGHT )
     */
    OFFHAND,

    // Body Main
    WAIST,
    CHEST,

    // Body Legs
    LEFT_FOOT,
    RIGHT_FOOT,

    LEFT_ANKLE,
    RIGHT_ANKLE,

    LEFT_KNEE,
    RIGHT_KNEE,

    // Body Arms
    LEFT_WRIST,
    RIGHT_WRIST,

    LEFT_ELBOW,
    RIGHT_ELBOW,

    LEFT_SHOULDER,
    RIGHT_SHOULDER,
}

package org.vmstudio.visor.api.common.player;

public enum VRTrackableBodyPart {
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
    OFFHAND
    //@TODO add full body tracking
}

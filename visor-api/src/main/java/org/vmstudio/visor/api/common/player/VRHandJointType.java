package org.vmstudio.visor.api.common.player;

/**
 * Hand skeleton joints, in {@code XR_EXT_hand_tracking} order
 */
public enum VRHandJointType {
    PALM,
    WRIST,

    THUMB_METACARPAL,
    THUMB_PROXIMAL,
    THUMB_DISTAL,
    THUMB_TIP,

    INDEX_METACARPAL,
    INDEX_PROXIMAL,
    INDEX_INTERMEDIATE,
    INDEX_DISTAL,
    INDEX_TIP,

    MIDDLE_METACARPAL,
    MIDDLE_PROXIMAL,
    MIDDLE_INTERMEDIATE,
    MIDDLE_DISTAL,
    MIDDLE_TIP,

    RING_METACARPAL,
    RING_PROXIMAL,
    RING_INTERMEDIATE,
    RING_DISTAL,
    RING_TIP,

    LITTLE_METACARPAL,
    LITTLE_PROXIMAL,
    LITTLE_INTERMEDIATE,
    LITTLE_DISTAL,
    LITTLE_TIP;

    private static final VRHandJointType[] VALUES = values();

    public static final int COUNT = VALUES.length;

    public static VRHandJointType fromIndex(int index) {
        return VALUES[index];
    }
}

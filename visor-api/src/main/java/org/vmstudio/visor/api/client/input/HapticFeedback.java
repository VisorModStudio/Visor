package org.vmstudio.visor.api.client.input;

/**
 * Pulse lengths for haptics Visor fires, in microseconds
 */
public final class HapticFeedback {

    public static final int UI_CLICK = 100;
    public static final int UI_SLICE_CHANGE = 500;

    public static final int SWING_BLOCK_PER_HIT = 250;
    public static final int SWING_ATTACK = 1_000;

    public static final int RIPTIDE_SPIN = 200;
    public static final int CLIMB_GRAB = 2_000;

    public static final int HOTBAR_SHOW = 2_000;
    public static final int HOTBAR_HIDE = 3_000;

    public static final int CONSUME_BITE = 7_000;
    public static final int FISHING_BITE = 5_000;

    public static final int WORLD_DOOR_HIT = 7_500;
    public static final int WORLD_ANVIL_USE = 5_000;
    public static final int WORLD_ANVIL_LAND = 12_500;

    public static final int DEATH = 2_000_000;

    private HapticFeedback() {
        throw new UnsupportedOperationException("Utility class");
    }
}

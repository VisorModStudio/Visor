package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.nbt.CompoundTag;

/**
 * Cross-mc-version helper for nbt tags
 */
public class McNbt {
    private McNbt() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static float getFloat(CompoundTag tag, String key, float fallback) {
        return tag.contains(key) ? tag.getFloat(key) : fallback;
    }

    public static int getInt(CompoundTag tag, String key, int fallback) {
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }
}

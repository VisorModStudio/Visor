package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Utils for client cross--mc-version methods
 */
public class McVersionClientUtils {
    private McVersionClientUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isLevelTransitionScreen(@Nullable Screen screen) {
        return screen instanceof ReceivingLevelScreen
                || screen instanceof ProgressScreen
                || screen instanceof GenericDirtMessageScreen;
    }
}

package org.vmstudio.visor.api.compatibility.mcversion;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;


@Environment(EnvType.CLIENT)
public class McVersionUtilsClient {
    private McVersionUtilsClient() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isLevelTransitionScreen(@Nullable Screen screen) {
        return screen instanceof ReceivingLevelScreen
                || screen instanceof ProgressScreen
                || screen instanceof GenericMessageScreen;
    }

}

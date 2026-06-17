package org.vmstudio.visor.api.client.gui.widgets;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public final class ClampedTooltipPositioner implements ClientTooltipPositioner {
    public static final ClampedTooltipPositioner INSTANCE = new ClampedTooltipPositioner();

    private static final int MARGIN = 4;
    private static final int CURSOR_OFFSET = 12;

    private ClampedTooltipPositioner() {
    }

    @Override
    public @NotNull Vector2ic positionTooltip(int screenWidth, int screenHeight,
                                              int mouseX, int mouseY,
                                              int width, int height) {
        Vector2i pos = new Vector2i(mouseX + CURSOR_OFFSET, mouseY - CURSOR_OFFSET);

        if (pos.x + width + MARGIN > screenWidth) {
            pos.x = mouseX - CURSOR_OFFSET - width;
        }
        pos.x = Math.max(MARGIN, Math.min(pos.x, screenWidth - width - MARGIN));

        pos.y = Math.max(MARGIN, Math.min(pos.y, screenHeight - height - MARGIN));

        return pos;
    }
}

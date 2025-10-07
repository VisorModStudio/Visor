package me.phoenixra.visor.api.client.gui.overlay.options;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class OverlayOptionsScreen<T extends OverlayOptionGroup<?>> extends Screen {
    protected final T optionCategory;


    protected int cursorBoundsX = -1;
    protected int cursorBoundsY = -1;
    protected int cursorBoundsWidth = -1;
    protected int cursorBoundsHeight = -1;
    protected OverlayOptionsScreen(@NotNull T optionGroup) {
        super(Component.empty());
        this.optionCategory = optionGroup;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        var bg = OverlayOptionTextures.BACKGROUND;
        bg.blit(
                guiGraphics,
                0, 0,
                width, height
        );
    }
}

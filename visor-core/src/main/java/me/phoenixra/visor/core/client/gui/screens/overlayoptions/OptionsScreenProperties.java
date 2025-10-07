package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.options.types.OverlayOptionsProperties;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class OptionsScreenProperties  extends OverlayOptionsScreen<OverlayOptionsProperties> {



    public OptionsScreenProperties(@NotNull OverlayOptionsProperties optionCategory) {
        super(optionCategory);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsProperties;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class OptionsScreenProperties  extends OptionsScreen<OverlayOptionsProperties> {



    public OptionsScreenProperties(@NotNull OverlayOptionsProperties optionCategory) {
        super(optionCategory, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {

    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
}

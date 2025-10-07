package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.options.types.OverlayOptionsIdentity;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Getter
public class OptionsScreenIdentity extends OverlayOptionsScreen<OverlayOptionsIdentity> {



    public OptionsScreenIdentity(@NotNull OverlayOptionsIdentity optionCategory) {
        super(optionCategory);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

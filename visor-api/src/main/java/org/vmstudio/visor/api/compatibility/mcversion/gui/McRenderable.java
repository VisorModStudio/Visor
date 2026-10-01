package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

/**
 * Cross-mc-version adapter for Renderable.
 */
@Environment(EnvType.CLIENT)
public interface McRenderable extends Renderable {

    // ------- STABLE API -------

    void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick);


    // ------- MC-VERSION SPECIFIC IMPLEMENTATION -------

    //? if >=26.1 {
    @Override
    default void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        render(guiGraphics, mouseX, mouseY, partialTick);
    }
    //?}
}

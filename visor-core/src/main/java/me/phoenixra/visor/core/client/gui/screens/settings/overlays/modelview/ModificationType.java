package me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview;

import net.minecraft.network.chat.Component;

public enum ModificationType {
    FORMULA_POSITION,
    FORMULA_ROTATION,
    SLIDERS_POSITION,
    SLIDERS_ROTATION,
    BY_OFFSET,
    BY_HAND;

    public Component getName(){
        return Component.translatable("visor.enums.overlaySettings.ModificationType."+name());
    }
}

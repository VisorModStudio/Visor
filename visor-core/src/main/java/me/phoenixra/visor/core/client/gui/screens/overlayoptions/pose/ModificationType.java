package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose;

import net.minecraft.network.chat.Component;

public enum ModificationType {
    FORMULA_POSITION,
    FORMULA_ROTATION,
    SLIDERS_POSITION,
    SLIDERS_ROTATION,
    BY_OFFSET,
    BY_HAND;

    public Component getName(){
        return Component.translatable("visor.overlay.options.pose.enum.ModificationType."+name());
    }
}

package me.phoenixra.visor.api.client.gui.overlay.options;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.helpers.TexturesHelper;
import net.minecraft.resources.ResourceLocation;

public interface OverlayOptionTextures {
    ResourceLocation RESOURCE_2 = new ResourceLocation(
            "visor:textures/gui/overlays/settings/general.png"
    );
    int TEX_WIDTH_2 = 256;
    int TEX_HEIGHT_2 = 188;

    GuiTexture BACKGROUND = new GuiTexture(
            TexturesHelper.getSolidColorTexture(AtumColor.immutable(190,190,190,255)),
            0, 0,
            1, 1
    );

    GuiTexture BLACK_TEXTURE = new GuiTexture(
            TexturesHelper.getSolidColorTexture(AtumColor.immutable(13,13,13,255)),
            0, 0, 1, 1
    );
    GuiTexture DARK_GRAY_TEXTURE = new GuiTexture(
            TexturesHelper.getSolidColorTexture(AtumColor.immutable(34,34,34,255)),
            0, 0, 1, 1
    );


    GuiTexture GENERAL_BUTTON = new GuiTexture(
            RESOURCE_2,
            103, 136,
            52, 15,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture GENERAL_BUTTON_HOVERED = new GuiTexture(
            RESOURCE_2,
            103, 168,
            52, 15,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture GENERAL_BUTTON_SELECTED = new GuiTexture(
            RESOURCE_2,
            103, 152,
            52, 15,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );

    GuiTexture SCROLL_BAR = new GuiTexture(
            TexturesHelper.getSolidColorTexture(AtumColor.immutable(150,150,150,255)),
            0, 0,
            1, 1
    );
    GuiTexture SCROLL_BAR_ACTIVE = new GuiTexture(
            TexturesHelper.getSolidColorTexture(AtumColor.immutable(190,190,190,255)),
            0, 0,
            1, 1
    );


    GuiTexture CHECKBOX_BUTTON = new GuiTexture(
            RESOURCE_2,
            242, 97,
            12, 12,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture CHECKBOX_BUTTON_HOVERED = new GuiTexture(
            RESOURCE_2,
            229, 97,
            12, 12,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture CHECKBOX_BUTTON_SELECTED = new GuiTexture(
            RESOURCE_2,
            216, 97,
            12, 12,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture CHECKBOX_BUTTON_HOVERED_SELECTED = new GuiTexture(
            RESOURCE_2,
            203, 97,
            12, 12,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
}

package me.phoenixra.visor.api.client.gui.overlays.options;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.helpers.TexturesHelper;
import net.minecraft.resources.ResourceLocation;

public interface OptionTextures {
    ResourceLocation RESOURCE_2 = new ResourceLocation(
            "visor:textures/gui/overlays/settings/general_2.png"
    );
    int TEX_WIDTH_2 = 119;
    int TEX_HEIGHT_2 = 153;

    GuiTexture BACKGROUND_256x256 = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/bg_general_256x256.png"
            ),
            0, 0,
            256, 256
    );
    GuiTexture BACKGROUND_256x128 = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/bg_general_256x128.png"
            ),
            0, 0,
            256, 128
    );
    GuiTexture BACKGROUND_175x256 = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/bg_general_175x256.png"
            ),
            0, 0,
            175, 256
    );
    GuiTexture BACKGROUND_128x256 = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/bg_general_128x256.png"
            ),
            0, 0,
            128, 256
    );

    GuiTexture BLACK_TEXTURE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(13,13,13,255)
    );
    GuiTexture LIGHT_GRAY_TEXTURE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(150, 150, 150, 255)
    );
    GuiTexture GRAY_TEXTURE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(34,34,34,255)
    );


    AtumColor HOVERED_HIGHLIGHT = AtumColor.immutable(74,106,136,255);

    AtumColor SELECTED_HIGHLIGHT = AtumColor.immutable(107,107,107,255);


    GuiTexture SCROLL_BAR = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(150,150,150,255)
    );
    GuiTexture SCROLL_BAR_ACTIVE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(190,190,190,255)
    );

    GuiTexture ARROW_BLACK_LEFT = new GuiTexture(
            RESOURCE_2,
            0, 140,
            13, 13,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );

    GuiTexture ARROW_BLACK_RIGHT = new GuiTexture(
            RESOURCE_2,
            15, 140,
            13, 13,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );

    GuiTexture ARROW_GRAY_LEFT = new GuiTexture(
            RESOURCE_2,
            30, 140,
            13, 13,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );

    GuiTexture ARROW_GRAY_RIGHT = new GuiTexture(
            RESOURCE_2,
            45, 140,
            13, 13,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );


}

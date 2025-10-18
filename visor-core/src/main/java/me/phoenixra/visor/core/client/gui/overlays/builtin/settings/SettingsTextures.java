package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.resources.ResourceLocation;

public interface SettingsTextures {

    ResourceLocation RESOURCE = new ResourceLocation(
            "visor:textures/gui/overlays/settings/main.png"
    );
    int TEX_WIDTH = 179;
    int TEX_HEIGHT = 188;

    GuiTexture FILTER_BACKGROUND = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/bg_main_filters.png"
            )
    );


    GuiTexture BUTTON_LOAD = new GuiTexture(
            RESOURCE,
            0, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_LOAD_HOVERED = new GuiTexture(
            RESOURCE,
            35, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_CLOSE = new GuiTexture(
            RESOURCE,
            110, 112,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_CLOSE_HOVERED = new GuiTexture(
            RESOURCE,
            145, 112,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_DRAG = new GuiTexture(
            RESOURCE,
            75, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_DRAG_HOVERED = new GuiTexture(
            RESOURCE,
            110, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_DRAG_SELECTED = new GuiTexture(
            RESOURCE,
            145, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_TAB_LEFT = new GuiTexture(
            RESOURCE,
            64, 24,
            115, 23,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_TAB_RIGHT = new GuiTexture(
            RESOURCE,
            64, 0,
            115, 23,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture FILTER_BLACK_BUTTON = new GuiTexture(
            RESOURCE,
            132, 54,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_BLACK_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            148, 54,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_BLACK_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            164, 54,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture FILTER_GRAY_BUTTON = new GuiTexture(
            RESOURCE,
            132, 70,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_GRAY_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            148, 70,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_GRAY_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            164, 70,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture CHECKBOX_BUTTON = new GuiTexture(
            RESOURCE,
            75, 112,
            12, 12,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            75, 125,
            12, 12,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            88, 112,
            12, 12,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_BUTTON_HOVERED_SELECTED = new GuiTexture(
            RESOURCE,
            88, 125,
            12, 12,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture REMOVE_BUTTON = new GuiTexture(
            RESOURCE,
            0, 91,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture REMOVE_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            16, 91,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture CANCEL_BUTTON = new GuiTexture(
            RESOURCE,
            0, 112,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CANCEL_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            35, 112,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture COPY_BUTTON = new GuiTexture(
            RESOURCE,
            0, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture COPY_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            18, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture COPY_BUTTON_INACTIVE = new GuiTexture(
            RESOURCE,
            36, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture PASTE_BUTTON = new GuiTexture(
            RESOURCE,
            0, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture PASTE_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            18, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture PASTE_BUTTON_INACTIVE = new GuiTexture(
            RESOURCE,
            36, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture LABEL_BUILT_IN = new GuiTexture(
            RESOURCE,
            88, 65,
            10, 10,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture LABEL_CUSTOM = new GuiTexture(
            RESOURCE,
            88, 54,
            10, 10,
            TEX_WIDTH, TEX_HEIGHT
    );



    GuiTexture BUTTON_SAVE_WARNING = new GuiTexture(
            RESOURCE,
            0, 54,
            83, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture CREATE_BUTTON_WARNING = new GuiTexture(
            RESOURCE,
            77, 91,
            102, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

}

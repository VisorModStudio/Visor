package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.resources.ResourceLocation;

public interface SettingsTextures {

    ResourceLocation RESOURCE = new ResourceLocation(
            "visor:textures/gui/overlays/settings/general.png"
    );
    int TEX_WIDTH = 256;
    int TEX_HEIGHT = 188;

    GuiTexture FILTER_BACKGROUND = new GuiTexture(
            new ResourceLocation(
                    "visor:textures/gui/overlays/settings/background_filters.png"
            )
    );


    GuiTexture BUTTON_LOAD = new GuiTexture(
            RESOURCE,
            35, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_LOAD_HOVERED = new GuiTexture(
            RESOURCE,
            0, 152,
            34, 34,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_CLOSE = new GuiTexture(
            RESOURCE,
            118, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_CLOSE_HOVERED = new GuiTexture(
            RESOURCE,
            118, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_DRAG = new GuiTexture(
            RESOURCE,
            64, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_DRAG_HOVERED = new GuiTexture(
            RESOURCE,
            64, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_DRAG_SELECTED = new GuiTexture(
            RESOURCE,
            64, 36,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture BUTTON_TAB_LEFT = new GuiTexture(
            RESOURCE,
            139, 24,
            115, 23,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_TAB_RIGHT = new GuiTexture(
            RESOURCE,
            139, 0,
            115, 23,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture LIST_ENTRY = new GuiTexture(
            RESOURCE,
            0, 68,
            99, 18,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture LIST_ENTRY_HOVERED = new GuiTexture(
            RESOURCE,
            0, 106,
            99, 18,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture LIST_ENTRY_SELECTED = new GuiTexture(
            RESOURCE,
            0, 87,
            99, 18,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture FILTER_BLACK_BUTTON = new GuiTexture(
            RESOURCE,
            0, 16,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_BLACK_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            0, 0,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_BLACK_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            0, 32,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture FILTER_GRAY_BUTTON = new GuiTexture(
            RESOURCE,
            32, 16,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_GRAY_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            32, 0,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture FILTER_GRAY_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            32, 32,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );


    GuiTexture CHECKBOX_ALL_BUTTON = new GuiTexture(
            RESOURCE,
            242, 110,
            12, 14,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_ALL_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            229, 110,
            12, 14,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_ALL_BUTTON_SELECTED = new GuiTexture(
            RESOURCE,
            216, 110,
            12, 14,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CHECKBOX_ALL_BUTTON_HOVERED_SELECTED = new GuiTexture(
            RESOURCE,
            203, 110,
            12, 14,
            TEX_WIDTH, TEX_HEIGHT
    );



    GuiTexture CREATE_BUTTON_WARNING = new GuiTexture(
            RESOURCE,
            0, 136,
            102, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture REMOVE_BUTTON = new GuiTexture(
            RESOURCE,
            16, 16,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture REMOVE_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            16, 0,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture CANCEL_BUTTON = new GuiTexture(
            RESOURCE,
            48, 16,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture CANCEL_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            48, 0,
            15, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture COPY_BUTTON = new GuiTexture(
            RESOURCE,
            82, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture COPY_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            82, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture COPY_BUTTON_INACTIVE = new GuiTexture(
            RESOURCE,
            82, 36,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture PASTE_BUTTON = new GuiTexture(
            RESOURCE,
            100, 18,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture PASTE_BUTTON_HOVERED = new GuiTexture(
            RESOURCE,
            100, 0,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture PASTE_BUTTON_INACTIVE = new GuiTexture(
            RESOURCE,
            100, 36,
            17, 17,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture BUTTON_SAVE_WARNING = new GuiTexture(
            RESOURCE,
            171, 152,
            83, 15,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture BUTTON_SAVE_WARNING_HOVERED = new GuiTexture(
            RESOURCE,
            171, 136,
            83, 15,
            TEX_WIDTH, TEX_HEIGHT
    );

    GuiTexture LABEL_BUILT_IN = new GuiTexture(
            RESOURCE,
            244, 79,
            10, 10,
            TEX_WIDTH, TEX_HEIGHT
    );
    GuiTexture LABEL_CUSTOM = new GuiTexture(
            RESOURCE,
            244, 68,
            10, 10,
            TEX_WIDTH, TEX_HEIGHT
    );
}

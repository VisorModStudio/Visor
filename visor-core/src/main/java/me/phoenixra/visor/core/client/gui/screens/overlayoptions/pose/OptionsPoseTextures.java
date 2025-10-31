package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import net.minecraft.resources.ResourceLocation;

public interface OptionsPoseTextures {

    ResourceLocation RESOURCE_2 = new ResourceLocation(
            "visor:textures/gui/overlays/settings/general_2.png"
    );
    int TEX_WIDTH_2 = 124;
    int TEX_HEIGHT_2 = 168;

    GuiTexture BUTTON_DEMO = new GuiTexture(
            RESOURCE_2,
            0, 0,
            40, 40,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_DEMO_ACTIVE = new GuiTexture(
            RESOURCE_2,
            0, 42,
            40, 40,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );


    GuiTexture BUTTON_EMULATE = new GuiTexture(
            RESOURCE_2,
            42, 0,
            40, 40,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_EMULATE_ACTIVE = new GuiTexture(
            RESOURCE_2,
            42, 42,
            40, 40,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_EMULATE_INACTIVE = new GuiTexture(
            RESOURCE_2,
            79, 86,
            40, 40,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );


    GuiTexture BUTTON_AIM = new GuiTexture(
            RESOURCE_2,
            84, 5,
            35, 35,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_AIM_ACTIVE = new GuiTexture(
            RESOURCE_2,
            84, 42,
            35, 35,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );


    GuiTexture BUTTON_DRAG = new GuiTexture(
            RESOURCE_2,
            0, 86,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_DRAG_ACTIVE = new GuiTexture(
            RESOURCE_2,
            0, 112,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );




    GuiTexture BUTTON_TELEPORT = new GuiTexture(
            RESOURCE_2,
            26, 86,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_TELEPORT_INACTIVE = new GuiTexture(
            RESOURCE_2,
            26, 112,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );



    GuiTexture BUTTON_APPLY_OFFSET = new GuiTexture(
            RESOURCE_2,
            52, 86,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
    GuiTexture BUTTON_APPLY_OFFSET_INACTIVE = new GuiTexture(
            RESOURCE_2,
            52, 112,
            24, 24,
            TEX_WIDTH_2, TEX_HEIGHT_2
    );
}

package me.phoenixra.visor.api.client;

import lombok.Getter;

public enum ClientFeature {

    /**
     * Movement modifiers.
     * it is what intercepts in a movement, like room tasks: climb,
     * jump, crawl
     */
    MOVEMENT_MODIFIERS(false),

    /**
     * Movement via input from VR
     */
    INPUT_MOVEMENT(false),

    /**
     * Mouse actions in VR
     */
    INPUT_MOUSE(false),


    /**
     * If HUD should not be rendered
     */
    GUI_DISABLE_HUD(false),

    /**
     * VR cursor processing and rendering
     */
    GUI_CURSOR(true),


    /**
     * Effects like cross-hair, block outline
     */
    AIM_EFFECTS(true),

    /**
     * VR Hands rendering
     */
    VR_HANDS(true),

    /**
     * VR World hands rendering. If false, GUI hands force rendered instead
     */
    VR_WORLD_HANDS(true),

    /**
     * VR World main hand rendering. If false, GUI hand force rendered instead
     */
    VR_WORLD_HAND_MAIN(true),

    /**
     * VR World offhand rendering. If false, GUI hand force rendered instead
     */
    VR_WORLD_HAND_OFFHAND(true);

    @Getter
    private boolean renderFeature;
    ClientFeature(boolean renderFeature){
        this.renderFeature = renderFeature;
    }

}

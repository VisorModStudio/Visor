package org.vmstudio.visor.api.client;

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
    AIM_EFFECTS(true);

    @Getter
    private boolean renderFeature;
    ClientFeature(boolean renderFeature){
        this.renderFeature = renderFeature;
    }

}

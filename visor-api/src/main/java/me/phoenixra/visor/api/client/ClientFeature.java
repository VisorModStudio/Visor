package me.phoenixra.visor.api.client;

import lombok.Getter;

public enum ClientFeature {

    /**
     * Movement modifiers.
     * it is what intercepts in a movement, like climb,
     * jump, crawl trackers
     */
    MOVEMENT_MODIFIERS(false),

    /**
     * Movement via VR Input actions
     */
    INPUT_MOVEMENT(false),

    INPUT_VR_MOUSE(false),

    /**
     * Effects like crosshair, block outline
     */
    AIM_EFFECTS(true),

    /**
     * VR Hands rendering
     */
    VR_HANDS(true);

    @Getter
    private boolean renderFeature;
    ClientFeature(boolean renderFeature){
        this.renderFeature = renderFeature;
    }

}

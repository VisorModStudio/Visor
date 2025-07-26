package me.phoenixra.visor.api.client.render;

import lombok.Getter;

public enum VRDisplay {
    GUI(false),
    EYE_LEFT(true),
    EYE_RIGHT(true),
    FIRST_PERSON(true),
    THIRD_PERSON(true);

    @Getter
    private final boolean world;

    VRDisplay(boolean world){
        this.world = world;
    }

    public boolean isEye(){
        return this == EYE_LEFT || this == EYE_RIGHT;
    }


    /**
     * Display that renders the VR world first and should to be used to update
     * render stuff to not update same for other displays
     * @return render display
     */
    public static VRDisplay worldUpdater(){
        return EYE_LEFT;
    }

}

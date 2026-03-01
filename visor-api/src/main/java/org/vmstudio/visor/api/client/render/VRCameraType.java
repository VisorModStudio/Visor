package org.vmstudio.visor.api.client.render;

import lombok.Getter;

public enum VRCameraType {
    GUI(false),
    EYE_LEFT(true),
    EYE_RIGHT(true),
    FIRST_PERSON(true),
    THIRD_PERSON(true);

    @Getter
    private final boolean world;

    VRCameraType(boolean world){
        this.world = world;
    }

    public boolean isEye(){
        return this == EYE_LEFT || this == EYE_RIGHT;
    }


    /**
     * VR camera that renders the VR world first and should to be used to update
     * render stuff to not update same for other cameras
     * @return VR camera type
     */
    public static VRCameraType worldUpdater(){
        return EYE_LEFT;
    }

}

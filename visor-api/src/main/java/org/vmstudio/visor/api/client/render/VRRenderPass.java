package org.vmstudio.visor.api.client.render;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;

public enum VRRenderPass {
    NULL(false),
    GUI(false),
    EYE_LEFT(true),
    EYE_RIGHT(true),
    CENTER(true),
    THIRD_PERSON(true);

    @Getter
    private final boolean world;

    VRRenderPass(boolean world){
        this.world = world;
    }

    public boolean isEye(){
        return this == EYE_LEFT || this == EYE_RIGHT;
    }

    public boolean isFirstPerson(){
        return this == EYE_LEFT || this == EYE_RIGHT || this == CENTER;
    }

    public boolean isThirdPerson(){
        return this == THIRD_PERSON;
    }

    public boolean isNull(){
        return this == NULL;
    }

    /**
     * VR render pass that renders the VR world first and should to be used to update
     * render stuff to not update same for other cameras
     * @return VR render pass
     */
    public static VRRenderPass worldUpdater(){
        return EYE_LEFT;
    }

    public EyeType getEyeOrLeft(){
        return this == EYE_LEFT
                ? EyeType.LEFT : this == EYE_RIGHT
                ? EyeType.RIGHT : EyeType.LEFT;
    }

}

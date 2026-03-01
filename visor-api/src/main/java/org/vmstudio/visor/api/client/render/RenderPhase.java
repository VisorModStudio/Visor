package org.vmstudio.visor.api.client.render;


public enum RenderPhase {
    VANILLA,
    VR_GUI,
    VR_WORLD,
    VR_MIRROR;


    public boolean isVanilla(){
        return this == VANILLA;
    }

    public boolean isNotVanilla(){
        return this != VANILLA;
    }



    public boolean isVRGui(){
        return this == VR_GUI;
    }

    public boolean isNotVRGui(){
        return this != VR_GUI;
    }



    public boolean isVRWorld(){
        return this == VR_WORLD;
    }

    public boolean isNotVRWorld(){
        return this != VR_WORLD;
    }



    public boolean isVRMirror(){
        return this == VR_MIRROR;
    }

    public boolean isNotVRMirror(){
        return this != VR_MIRROR;
    }

}

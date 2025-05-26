package me.phoenixra.visor.api.client;

import net.minecraft.client.Minecraft;


public enum VRPlayMode {
    /**
     * VR session initialized and VR is active
     * when headset is on or always active
     * if server does not allow switching to vanilla
     */
    ENABLED,

    /**
     * no VR session initialized
     */
    DISABLED,

    /**
     * VR session initialized, but VR active only in world
     */
    WORLD_ONLY;


    public VRPlayMode next(){
        return switch (this){
            case ENABLED -> DISABLED;
            case DISABLED -> WORLD_ONLY;
            case WORLD_ONLY -> ENABLED;
        };
    }

    public boolean canPlayVR(){
        if(this == VRPlayMode.WORLD_ONLY){
            Minecraft mc = Minecraft.getInstance();
            return  mc.level != null;
        }else{
            return this == VRPlayMode.ENABLED;
        }
    }

    public boolean canInitVR(){
        return this == VRPlayMode.WORLD_ONLY || this == VRPlayMode.ENABLED;
    }
}

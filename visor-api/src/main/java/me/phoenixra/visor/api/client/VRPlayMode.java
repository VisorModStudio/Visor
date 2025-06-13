package me.phoenixra.visor.api.client;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.ApiStatus;


public enum VRPlayMode {

    /**
     * <p>Behaviour:</p>
     * <ul>
     *   <li>VR session initializes on startup and is playable from the main menu.</li>
     *   <li>Requires a VR runtime &amp; hardware.</li>
     * </ul>
     */
    ENABLED,

    /**
     * <p>Behaviour:</p>
     * <ul>
     *   <li>VR session never initializes.</li>
     *   <li>Does <strong>not</strong> require VR runtime or hardware. </li>
     *   <li>Only modifies rendering of <strong>remote</strong> VR players (if connected to a server/plugin supporting Visor).</li>
     * </ul>
     */
    DISABLED,

    /**
     * <p>Behaviour:</p>
     * <ul>
     *   <li>VR session initializes on startup but is playable only once you’re <strong>in-world</strong>.</li>
     *   <li>Requires a VR runtime &amp; hardware.</li>
     * </ul>
     */
    WORLD_ONLY;


    /**
     * Returns whether a VR session can be initialized.
     *
     * @return true/false
     */
    public boolean canInitVR(){
        return this == VRPlayMode.WORLD_ONLY || this == VRPlayMode.ENABLED;
    }

    /**
     * Returns whether VR can be activated to play
     *
     * @return true/false
     */
    public boolean canPlayVR(){
        if(this == VRPlayMode.WORLD_ONLY){
            Minecraft mc = Minecraft.getInstance();
            return  mc.level != null;
        }else{
            return this == VRPlayMode.ENABLED;
        }
    }



    /**
     * Cycles to the next play mode in the order:
     * {@link #ENABLED} → {@link #DISABLED} → {@link #WORLD_ONLY} → {@link #ENABLED}.
     *
     * @return the next {@link VRPlayMode}
     */
    public VRPlayMode next(){
        return switch (this){
            case ENABLED -> DISABLED;
            case DISABLED -> WORLD_ONLY;
            case WORLD_ONLY -> ENABLED;
        };
    }
}

package org.vmstudio.visor.api.client;

import net.minecraft.client.Minecraft;


/**
 * Defines whether the VR session
 * is initialized and when VR is active.
 *
 */
public enum VRPlayMode {

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
     *   <li>VR session initializes on startup and is playable from the main menu.</li>
     *   <li>Requires a VR runtime &amp; hardware.</li>
     * </ul>
     */
    ENABLED,


    /**
     * <p>Behaviour:</p>
     * <ul>
     *   <li>VR session initializes on startup but is playable only once you’re <strong>in-world</strong>.</li>
     *   <li>Requires a VR runtime &amp; hardware.</li>
     * </ul>
     */
    WORLD_ONLY,

    /**
     * <p>Behaviour:</p>
     * <ul>
     *   <li>VR session initializes on startup and is always active</li>
     *   <li>Requires a VR runtime &amp; hardware.</li>
     * </ul>
     */
    ALWAYS_ACTIVE;


    /**
     * Returns whether a VR session can be initialized.
     *
     * @return true/false
     */
    public boolean canInitVR(){
        return this == VRPlayMode.WORLD_ONLY || this == VRPlayMode.ENABLED || this == ALWAYS_ACTIVE;
    }

    /**
     * Returns whether VR can be activated to play
     *
     * @return true/false
     */
    public boolean canPlayVR(){
        if(this == WORLD_ONLY){
            Minecraft mc = Minecraft.getInstance();
            return  mc.level != null;
        }else{
            return this == ENABLED || this == ALWAYS_ACTIVE;
        }
    }



    /**
     * Cycles to the next play mode in the order:
     * {@link #ENABLED} -> {@link #ALWAYS_ACTIVE} -> {@link #WORLD_ONLY} -> {@link #DISABLED}.
     *
     * @return the next {@link VRPlayMode}
     */
    public VRPlayMode next(){
        return switch (this){
            case ENABLED -> WORLD_ONLY;
            case WORLD_ONLY -> ALWAYS_ACTIVE;
            case ALWAYS_ACTIVE -> DISABLED;
            case DISABLED -> ENABLED;
        };
    }

    /**
     * Cycles to the previous play mode in the order:
     * {@link #ENABLED} <- {@link #ALWAYS_ACTIVE} <- {@link #WORLD_ONLY} <- {@link #DISABLED}.
     *
     * @return the previous {@link VRPlayMode}
     */
    public VRPlayMode previous(){
        return switch (this){
            case ENABLED -> DISABLED;
            case DISABLED -> ALWAYS_ACTIVE;
            case ALWAYS_ACTIVE -> WORLD_ONLY;
            case WORLD_ONLY -> ENABLED;
        };
    }
}

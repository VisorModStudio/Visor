package org.vmstudio.visor.api.client;

/**
 * Defines state of the VR session
 *
 */
public enum VRStateMode {

    /**
     * No VR session is present.
     * <br>
     * All behaviors fall back to vanilla.
     */
    OFF,

    /**
     * VR session created and ready, but not yet rendering.
     * <br>
     * All behaviors fall back to vanilla.
     */
    INITIALIZED,

    /**
     * VR session is rendering
     *
     * <p>In that state, VR is partially functional,
     * some features may be inactive to not waste resources
     * or cause unexpected/unwanted behaviour</p>
     */
    ACTIVE,

    /**
     * VR session is focused.
     *
     * <p>In that state, VR is fully functional</p>
     */
    FOCUSED;

    /**
     * If the VR state is at least {@link #INITIALIZED}.
     *
     * @return true when mode is INITIALIZED, ACTIVE, or FOCUSED
     */
    public boolean isInitialized(){
        return this != OFF;
    }

    /**
     * Opposite of {@link #isInitialized()}
     * @return true when the mode is DISABLED
     */
    public boolean isNotInitialized(){
        return !isInitialized();
    }

    /**
     * If the VR session is rendering.
     *
     * @return true when mode is ACTIVE or FOCUSED
     */
    public boolean isActive(){
        return this == ACTIVE || this == FOCUSED;
    }

    /**
     * Opposite of {@link #isActive()}
     * @return true when not ACTIVE or FOCUSED
     */
    public boolean isNotActive(){
        return !isActive();
    }

    /**
     * If the VR session is focused
     *
     * @return true when mode is FOCUSED
     */
    public boolean isFocused(){
        return this == FOCUSED;
    }

    /**
     * Opposite of {@link #isFocused()}
     * @return true when not FOCUSED
     */
    public boolean isNotFocused(){
        return !isFocused();
    }

}

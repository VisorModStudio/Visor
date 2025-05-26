package me.phoenixra.visor.api.client;

public enum VRStateMode {

    /**
     * No VR session is present; all behaviors fall back to vanilla.
     */
    DISABLED,

    /**
     * VR session created and ready, but not yet rendering.
     */
    INITIALIZED,

    /**
     * VR session is rendering
     */
    ACTIVE,

    /**
     * VR session is focused
     */
    FOCUSED;

    /**
     * If the VR state is at least {@link #INITIALIZED}.
     *
     * @return true when mode is INITIALIZED, ACTIVE, or FOCUSED
     */
    public boolean isInitialized(){
        return this != DISABLED;
    }

    /**
     * Opposite of {@link #isInitialized()}
     * @return true when DISABLED
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

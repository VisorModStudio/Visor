package org.vmstudio.visor.api.client.tasks;

public enum TaskType {

    /**
     * Run on VR client tick.
     * Does not provide player instance
     */
    VR_PRE_TICK,

    /**
     * Run on VR player tick.
     * Method tick() for task will
     * provide not-null player instance
     */
    VR_PLAYER_TICK,

    /**
     * Run on VR render
     * provide nullable player instance
     */
    VR_PRE_RENDER
}

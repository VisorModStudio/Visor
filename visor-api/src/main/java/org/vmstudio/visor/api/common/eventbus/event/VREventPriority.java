package org.vmstudio.visor.api.common.eventbus.event;

public enum VREventPriority {


    LOWEST,

    LOW,

    NORMAL,

    HIGH,

    HIGHEST,

    /**
     * At this phase, the event cannot be modified
     */
    MONITOR;

    public VREventPriority next(){
        return switch (this){
            case LOWEST -> LOW;
            case LOW -> NORMAL;
            case NORMAL -> HIGH;
            case HIGH -> HIGHEST;
            case HIGHEST, MONITOR -> MONITOR;

        };
    }
}

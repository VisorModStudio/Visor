package org.vmstudio.visor.api.common.addon.component;

import lombok.Getter;

/**
 * Standard priority levels for {@link VisorComponent}
 */
public enum ComponentPriority {
    HIGHEST(6),
    HIGHER(5),
    HIGH(4),
    NORMAL(3),
    LOW(2),
    LOWER(1),
    LOWEST(0);

    @Getter
    private final int weight;

    ComponentPriority(int weight) {
        this.weight = weight;
    }

}

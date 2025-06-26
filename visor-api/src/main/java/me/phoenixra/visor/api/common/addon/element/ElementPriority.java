package me.phoenixra.visor.api.common.addon.element;

import lombok.Getter;

public enum ElementPriority {
    HIGHEST(6),
    HIGHER(5),
    HIGH(4),
    NORMAL(3),
    LOW(2),
    LOWER(1),
    LOWEST(0);

    @Getter
    private final int weight;

    ElementPriority(int weight) {
        this.weight = weight;
    }

}

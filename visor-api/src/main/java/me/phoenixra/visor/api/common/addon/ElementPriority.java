package me.phoenixra.visor.api.common.addon;

import lombok.Getter;

public enum ElementPriority {
    HIGH(3),
    NORMAL(2),
    LOW(1),
    FALLBACK(0);

    @Getter
    private final int weight;

    ElementPriority(int weight) {
        this.weight = weight;
    }

}

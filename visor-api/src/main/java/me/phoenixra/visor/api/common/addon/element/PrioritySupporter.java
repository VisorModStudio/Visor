package me.phoenixra.visor.api.common.addon.element;

import org.jetbrains.annotations.NotNull;

public interface PrioritySupporter extends Comparable<PrioritySupporter>{

    @NotNull
    ElementPriority getPriority();


    @Override
    default int compareTo(@NotNull PrioritySupporter o) {
        return Integer.compare(
                o.getPriority().getWeight(),
                getPriority().getWeight()
        );
    }
}

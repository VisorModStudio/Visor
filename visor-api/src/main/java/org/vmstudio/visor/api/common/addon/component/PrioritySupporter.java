package org.vmstudio.visor.api.common.addon.component;

import org.jetbrains.annotations.NotNull;

/**
 * Marks {@link VisorComponent} that can be ordered by priority.
 */
public interface PrioritySupporter extends Comparable<PrioritySupporter>{

    /**
     * @return priority value
     */
    @NotNull
    ComponentPriority getPriority();



    @Override
    default int compareTo(@NotNull PrioritySupporter o) {
        return Integer.compare(
                o.getPriority().getWeight(),
                getPriority().getWeight()
        );
    }
}

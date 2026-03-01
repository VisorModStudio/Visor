package org.vmstudio.visor.api.client.gui.widgets.lists;

import me.phoenixra.atumconfig.api.tuples.PairRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FilterListWidget<T> {

    @NotNull FilterListType getType();


    void setOnFilterChanged(@NotNull Consumer<PairRecord<String, Boolean>> consumer);

    boolean filter(@NotNull T item);

    @NotNull Collection<String> getFilterIds();
    @NotNull Collection<String> getActiveFilterIds();

    @NotNull Collection<Function<T, Boolean>> getActiveFilters();
    @NotNull Collection<Function<T, Boolean>> getAllFilters();

}

package org.vmstudio.visor.api.client.gui.widgets.sets;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoCheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoImage;
import org.vmstudio.visor.api.client.gui.widgets.lists.CheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.lists.FilterListType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FiltersListWidgetSet<T> implements FilterListWidgetSet<T> {
    private final Minecraft mc;

    @Getter
    private final FilterListType type;

    private final WidgetInfoImage backgroundInfo;

    private final WidgetInfoEditBox searchBoxInfo;
    private final WidgetInfoButtonImaged checkboxAllInfo;

    private final WidgetInfoCheckboxList listInfo;


    //[entry id, value]
    private final Map<String, String> rawEntries;
    //[entry id, filter]
    private final Map<String, Function<T, Boolean>> filtersMap;
    //[id list]
    private final Supplier<List<String>> selectedSupplier;


    @Setter
    private Consumer<PairRecord<String, Boolean>> onFilterChanged;


    private EditBoxImaged searchBox;
    private ButtonImaged checkboxAll;
    @Getter
    private CheckboxList list;


    private boolean updateCheckBox;
    private FiltersListWidgetSet(Builder<T> builder){
        this.mc = Minecraft.getInstance();
        this.type = builder.type;
        this.backgroundInfo = builder.backgroundInfo;

        this.searchBoxInfo = builder.searchBoxInfo;
        this.checkboxAllInfo = builder.checkboxAllInfo;
        this.listInfo = builder.listInfo;

        this.rawEntries = builder.rawEntries;
        this.selectedSupplier = builder.selectedSupplier;
        this.filtersMap = builder.filtersMap;
    }
    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(backgroundInfo != null){
            GuiTexture texture = backgroundInfo.getTexture();
            texture.blit(
                    guiGraphics,
                    backgroundInfo.getX(), backgroundInfo.getY(),
                    backgroundInfo.getWidth(), backgroundInfo.getHeight()
            );
        }
        if(checkboxAll != null && updateCheckBox) {
            updateCheckBox = false;
            if(list.isAllSelected()) {
                checkboxAll.setSelected(true);
            }else if(list.isAllNotSelected()){
                checkboxAll.setSelected(false);
            }
        }
    }

    @Override
    public void onTick() {
    }

    @Override
    public <W extends GuiEventListener
            & Renderable
            & NarratableEntry> List<W> initWidgets() {
        if(checkboxAllInfo != null) {
            checkboxAll = new ButtonImaged(
                    checkboxAllInfo,
                    (button) -> {
                        button.setSelected(!button.isSelected());
                        list.changeSelectedAll(button.isSelected());
                    }
            );
        }

        if(searchBoxInfo != null) {
            searchBox = new EditBoxImaged(
                    searchBoxInfo
            );
            searchBox.setResponder(this::applySearchFilter);
        }

        list = new CheckboxList(
                listInfo,
                rawEntries,
                selectedSupplier.get(),
                (it)->{
                    updateCheckBox = true;
                    if(onFilterChanged != null){
                        onFilterChanged.accept(
                                new PairRecord<>(it.getId(), it.isSelected())
                        );
                    }
                }

        );

        if(checkboxAll != null) {
            if(list.isAllSelected()) {
                checkboxAll.setSelected(true);
            }else if(list.isAllNotSelected()){
                checkboxAll.setSelected(false);
            }
        }
        return getWidgets();
    }

    @Override
    public <W extends GuiEventListener
            & Renderable
            & NarratableEntry> List<W> getWidgets() {
        List<W> widgets = new ArrayList<>();
        widgets.add((W) list);
        if(searchBox != null){
            widgets.add((W) searchBox);
        }
        if(checkboxAll != null){
            widgets.add((W) checkboxAll);
        }
        return widgets;
    }

    private void applySearchFilter(String filterText) {
        String normalizedFilter = normalizeText(filterText);
        if (normalizedFilter.isEmpty()) {
            list.filterEntries(it->true);
        } else {
            list.filterEntries(entry -> {
                String value = entry.getValue();
                return value != null
                        && normalizeText(value).contains(normalizedFilter);
            });
        }
    }
    private String normalizeText(String input) {
        if (input == null) return "";
        String noAccents = Normalizer
                .normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT).trim();
    }

    @Override
    public boolean filter(@NotNull T item) {
        if(type == FilterListType.ALL) {
            for (var entry : getActiveFilters()) {
                if (!entry.apply(item)) {
                    return false;
                }
            }
            return true;
        }else{
            for (var entry : getActiveFilters()) {
                if (entry.apply(item)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public @NotNull Collection<String> getFilterIds() {
        return filtersMap.keySet();
    }
    @Override
    public @NotNull Collection<String> getActiveFilterIds() {
        return list.getSelectedEntriesId();
    }

    @Override
    public @NotNull Collection<Function<T, Boolean>> getActiveFilters(){
        var selected = list.getSelectedEntriesId();
        return filtersMap.entrySet()
                .stream()
                .filter(it->selected.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    @Override
    public @NotNull Collection<Function<T, Boolean>> getAllFilters() {
        return filtersMap.values();
    }

    public static class Builder<T> {
        private final FilterListType type;

        private WidgetInfoImage backgroundInfo;

        private WidgetInfoEditBox searchBoxInfo;
        private WidgetInfoButtonImaged checkboxAllInfo;


        private WidgetInfoCheckboxList listInfo;

        private final Map<String, String> rawEntries;
        private final Map<String, Function<T, Boolean>> filtersMap;
        private final Supplier<List<String>> selectedSupplier;

        public Builder(@NotNull FilterListType type,
                       @NotNull WidgetInfoCheckboxList listInfo,
                       @NotNull Map<String, String> rawEntries,
                       @NotNull Map<String, Function<T, Boolean>> filtersMap,
                       @NotNull Supplier<List<String>> selectedSupplier) {
            this.type = type;
            this.listInfo = listInfo;
            this.rawEntries = rawEntries;
            this.selectedSupplier = selectedSupplier;
            this.filtersMap = filtersMap;
        }

        public Builder<T> background(@Nullable WidgetInfoImage backgroundInfo) {
            this.backgroundInfo = backgroundInfo;
            return this;
        }

        public FiltersListWidgetSet.Builder<T> searchBox(@Nullable WidgetInfoEditBox searchBoxInfo) {
            this.searchBoxInfo = searchBoxInfo;
            return this;
        }

        public FiltersListWidgetSet.Builder<T> checkboxAll(@Nullable WidgetInfoButtonImaged checkboxAllInfo) {
            this.checkboxAllInfo = checkboxAllInfo;
            return this;
        }




        public FiltersListWidgetSet<T> build() {
            return new FiltersListWidgetSet<>(this);
        }
    }
}

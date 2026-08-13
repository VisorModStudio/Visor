package org.vmstudio.visor.api.client.gui.widgets.sets;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class SearchableListWidgetSet extends DynamicWidgetSet{
    private final Minecraft mc;


    private final WidgetInfoSelectionList listInfo;
    private final WidgetInfoButtonImaged filterInfo;
    private final WidgetInfoEditBox searchBoxInfo;

    @Getter
    private final FilterListWidgetSet<String> filterWidgetSet;


    private final Map<String, String> rawEntries;
    private final Consumer<TexturedSelectionList.TexturedEntry> onSelected;

    @Getter
    private ButtonImaged filterButton;
    @Getter
    private EditBoxImaged searchBox;
    @Getter
    private TexturedSelectionList list;


    private SearchableListWidgetSet(Builder builder) {
        super(builder.onWidgetsChanged);
        this.mc = Minecraft.getInstance();
        this.listInfo= builder.listInfo;
        this.filterInfo = builder.filterInfo;
        this.searchBoxInfo = builder.searchBoxInfo;

        this.rawEntries = builder.rawEntries;
        this.onSelected = builder.onSelected;
        this.filterWidgetSet = builder.filterWidgetSet;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        if(filterButton != null
                && filterWidgetSet != null
                && filterButton.isSelected()){
            filterWidgetSet.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onTick() {
        if(filterButton != null
                && filterWidgetSet != null
                && filterButton.isSelected()){
            filterWidgetSet.onTick();
        }
    }

    @Override
    public  <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T>  initWidgets() {

        if(filterInfo != null) {
            filterButton = new ButtonImaged(
                    filterInfo,
                    (it)->{
                        it.setSelected(!it.isSelected());
                        widgetsChanged();
                    }
            );
            if(filterWidgetSet != null){
                filterWidgetSet.initWidgets();
                filterWidgetSet.setOnFilterChanged(
                        (it)->{
                            String searchText = "";
                            if(searchBox != null){
                                searchText = searchBox.getValue();
                            }
                            applyFilter(
                                    searchText
                            );
                        }
                );
            }
        }

        if(searchBoxInfo != null) {
            searchBox = new EditBoxImaged(
                    searchBoxInfo
            );
            searchBox.setResponder(this::applyFilter);
        }

        list = new TexturedSelectionList(
                listInfo,
                rawEntries,
                onSelected

        );

        applyFilter(
                ""
        );

        return getWidgets();
    }


    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> widgets = new ArrayList<>();
        widgets.add((T) list);
        if(filterButton != null){
            widgets.add((T) filterButton);
            if(filterWidgetSet != null
                    && filterButton.isSelected()) {
                widgets.addAll(filterWidgetSet.getWidgets());
            }
        }
        if(searchBox != null){
            widgets.add((T) searchBox);
        }

        return widgets;
    }




    private void applyFilter(String filterText) {
        String normalizedFilter = normalizeText(filterText);
        list.filterEntries(entry -> {
            String id = entry.getKey();
            String value = entry.getValue();
            if(!normalizedFilter.isEmpty()
                    && !normalizeText(value).contains(normalizedFilter)){
                return false;
            }
            if(filterWidgetSet == null){
                return true;
            }

            return filterWidgetSet.filter(id);
        });
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        String noAccents = Normalizer
                .normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toLowerCase(Locale.ROOT).trim();
    }

    public static class Builder {
        private final Runnable onWidgetsChanged;

        private WidgetInfoSelectionList listInfo;
        private final  Map<String, String> rawEntries;
        private final Consumer<TexturedSelectionList.TexturedEntry> onSelected;

        private WidgetInfoButtonImaged filterInfo;
        private FilterListWidgetSet<String> filterWidgetSet;

        private WidgetInfoEditBox searchBoxInfo;

        public Builder(@NotNull WidgetInfoSelectionList listInfo,
                       @NotNull Map<String, String> rawEntries,
                       @NotNull Consumer<TexturedSelectionList.TexturedEntry> onSelected,
                       @NotNull Runnable onWidgetsChanged) {
            this.listInfo = listInfo;
            this.rawEntries = rawEntries;
            this.onSelected = onSelected;
            this.onWidgetsChanged = onWidgetsChanged;
        }

        public Builder filterButton(@NotNull WidgetInfoButtonImaged filterInfo,
                                    FilterListWidgetSet<String> filterWidgetSet) {
            this.filterInfo = filterInfo;
            this.filterWidgetSet = filterWidgetSet;
            return this;
        }
        /**
         * Position & size of the search box
         */
        public Builder searchBox(WidgetInfoEditBox searchBoxInfo) {
            this.searchBoxInfo = searchBoxInfo;
            return this;
        }

        /**
         * Builds the configured widget set
         */
        public SearchableListWidgetSet build() {
            return new SearchableListWidgetSet(this);
        }
    }
}

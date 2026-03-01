package org.vmstudio.visor.api.client.gui.widgets.sets;

import lombok.Getter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import org.vmstudio.visor.api.client.gui.widgets.lists.FilterListType;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoCheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterListBinaryWidgetSet<T> extends DynamicWidgetSet implements FilterListWidgetSet<T> {

    private final Minecraft mc;


    private final WidgetInfoEditBox searchBoxInfo;
    private final WidgetInfoButtonImaged checkboxAllInfo;

    private final WidgetInfoCheckboxList listInfo;

    @Getter
    private final FilterListType typeFirst;
    @Getter
    private final FilterListType typeSecond;

    private final WidgetInfoImage backgroundInfoFirst;
    private final WidgetInfoButtonImaged buttonInfoFirst;

    private final WidgetInfoImage backgroundInfoSecond;
    private final WidgetInfoButtonImaged buttonInfoSecond;


    //[entry id, value]
    private final Map<String, String> rawEntriesFirst;
    //[id list]
    private final Supplier<List<String>> selectedSupplierFirst;
    //[entry id, filter]
    private final Map<String, Function<T, Boolean>> filtersMapFirst;

    //[entry id, value]
    private final Map<String, String> rawEntriesSecond;
    //[id list]
    private final Supplier<List<String>> selectedSupplierSecond;
    //[entry id, filter]
    private final Map<String, Function<T, Boolean>> filtersMapSecond;


    @Getter
    private FiltersListWidgetSet<T> filtersWidgetFirst;
    @Getter
    private FiltersListWidgetSet<T> filtersWidgetSecond;
    private ButtonImaged buttonFirst;
    private ButtonImaged buttonSecond;


    private FilterListBinaryWidgetSet(Builder<T> builder) {
        super(builder.onWidgetsChanged);
        this.mc = Minecraft.getInstance();
        this.searchBoxInfo = builder.searchBoxInfo;
        this.checkboxAllInfo = builder.checkboxAllInfo;
        this.listInfo = builder.listInfo;

        this.typeFirst = builder.typeFirst;
        this.typeSecond = builder.typeSecond;

        this.backgroundInfoFirst = builder.backgroundInfoFirst;
        this.buttonInfoFirst = builder.buttonInfoFirst;
        this.rawEntriesFirst = builder.rawEntriesFirst;
        this.filtersMapFirst = builder.filtersMapFirst;
        this.selectedSupplierFirst = builder.selectedSupplierFirst;

        this.backgroundInfoSecond = builder.backgroundInfoSecond;
        this.buttonInfoSecond = builder.buttonInfoSecond;
        this.rawEntriesSecond = builder.rawEntriesSecond;
        this.filtersMapSecond = builder.filtersMapSecond;
        this.selectedSupplierSecond = builder.selectedSupplierSecond;
    }
    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(buttonFirst.isSelected()){
            filtersWidgetFirst.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        }else{
            filtersWidgetSecond.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void onTick() {
        if(buttonFirst.isSelected()){
            filtersWidgetFirst.onTick();
        }else{
            filtersWidgetSecond.onTick();
        }
    }

    @Override
    public <W extends GuiEventListener & Renderable & NarratableEntry> List<W> initWidgets() {
        filtersWidgetFirst = new FiltersListWidgetSet.Builder<>(
                typeFirst,
                listInfo,
                rawEntriesFirst,
                filtersMapFirst, selectedSupplierFirst
        ).background(backgroundInfoFirst)
                .checkboxAll(checkboxAllInfo)
                .searchBox(searchBoxInfo)
                .build();

        buttonFirst = new ButtonImaged(
                buttonInfoFirst,
                (it)-> changeActiveFilterList(true)
        );

        filtersWidgetSecond = new FiltersListWidgetSet.Builder<>(
                typeSecond,
                listInfo,
                rawEntriesSecond,
                filtersMapSecond, selectedSupplierSecond
        ).background(backgroundInfoSecond)
                .checkboxAll(checkboxAllInfo)
                .searchBox(searchBoxInfo)
                .build();
        buttonSecond = new ButtonImaged(
                buttonInfoSecond,
                (it)-> changeActiveFilterList(false)
        );

        buttonFirst.setSelected(true);
        filtersWidgetFirst.initWidgets();
        filtersWidgetSecond.initWidgets();

        return getWidgets();
    }

    @Override
    public <W extends GuiEventListener
            & Renderable
            & NarratableEntry> List<W> getWidgets() {
        var widgets = new ArrayList<W>();
        widgets.addAll(getActiveFiltersWidget().getWidgets());
        widgets.add((W) buttonFirst);
        widgets.add((W) buttonSecond);
        return widgets;
    }

    public void changeActiveFilterList(boolean useFirst){
        if(useFirst && buttonFirst.isSelected()) return;
        if(useFirst){
            buttonFirst.setSelected(true);
            buttonSecond.setSelected(false);
        }else{
            buttonFirst.setSelected(false);
            buttonSecond.setSelected(true);
        }
        widgetsChanged();
    }
    public FiltersListWidgetSet<T> getActiveFiltersWidget(){
        return buttonFirst.isSelected() ? filtersWidgetFirst : filtersWidgetSecond;
    }

    @Override
    public void setOnFilterChanged(@NotNull Consumer<PairRecord<String, Boolean>> consumer) {
        filtersWidgetFirst.setOnFilterChanged(consumer);
        filtersWidgetSecond.setOnFilterChanged(consumer);
    }



    @Override
    public @NotNull Collection<String> getFilterIds() {
        var list = new ArrayList<String>();
        list.addAll(filtersWidgetFirst.getFilterIds());
        list.addAll(filtersWidgetSecond.getFilterIds());
        return list;
    }

    @Override
    public @NotNull Collection<String> getActiveFilterIds() {
        var list = new ArrayList<String>();
        list.addAll(filtersWidgetFirst.getActiveFilterIds());
        list.addAll(filtersWidgetSecond.getActiveFilterIds());
        return list;
    }

    @Override
    public @NotNull Collection<Function<T, Boolean>> getActiveFilters() {
        var list = new ArrayList<Function<T, Boolean>>();
        list.addAll(filtersWidgetFirst.getActiveFilters());
        list.addAll(filtersWidgetSecond.getActiveFilters());
        return list;
    }

    @Override
    public @NotNull Collection<Function<T, Boolean>> getAllFilters() {
        var list = new ArrayList<Function<T, Boolean>>();
        list.addAll(filtersWidgetFirst.getAllFilters());
        list.addAll(filtersWidgetSecond.getAllFilters());
        return list;
    }

    @Override
    public boolean filter(@NotNull T item) {
        if(!filtersWidgetFirst.filter(item)){
             return false;
        }

        return filtersWidgetSecond.filter(item);
    }

    @Override
    public @NotNull FilterListType getType() {
        return buttonFirst.isSelected() ? typeFirst : typeSecond;
    }

    public static class Builder<T> {


        private WidgetInfoEditBox searchBoxInfo;
        private WidgetInfoButtonImaged checkboxAllInfo;
        private final WidgetInfoCheckboxList listInfo;

        private final Runnable onWidgetsChanged;

        private FilterListType typeFirst;
        private WidgetInfoImage backgroundInfoFirst;
        private WidgetInfoButtonImaged buttonInfoFirst;
        private Map<String, String> rawEntriesFirst;
        private Map<String, Function<T, Boolean>> filtersMapFirst;
        private Supplier<List<String>> selectedSupplierFirst;

        private FilterListType typeSecond;
        private WidgetInfoImage backgroundInfoSecond;
        private WidgetInfoButtonImaged buttonInfoSecond;
        private Map<String, String> rawEntriesSecond;
        private Map<String, Function<T, Boolean>> filtersMapSecond;
        private Supplier<List<String>> selectedSupplierSecond;


        public Builder(@NotNull WidgetInfoCheckboxList listInfo,
                       @NotNull Runnable onWidgetsChanged) {
            this.listInfo = listInfo;
            this.onWidgetsChanged = onWidgetsChanged;
        }

        public Builder<T> first(@NotNull FilterListType type,
                                @Nullable WidgetInfoImage backgroundInfo,
                                @NotNull WidgetInfoButtonImaged buttonInfo,
                                @NotNull Map<String, String> rawEntries,
                                @NotNull Map<String, Function<T, Boolean>> filtersMap,
                                @NotNull Supplier<List<String>> selectedSupplier) {
            this.typeFirst = type;
            this.backgroundInfoFirst = backgroundInfo;
            this.buttonInfoFirst = buttonInfo;
            this.rawEntriesFirst = rawEntries;
            this.filtersMapFirst = filtersMap;
            this.selectedSupplierFirst = selectedSupplier;
            return this;
        }
        public Builder<T> second(@NotNull FilterListType type,
                                 @Nullable WidgetInfoImage backgroundInfo,
                                 @NotNull WidgetInfoButtonImaged buttonInfo,
                                 @NotNull Map<String, String> rawEntries,
                                 @NotNull Map<String, Function<T, Boolean>> filtersMap,
                                 @NotNull Supplier<List<String>> selectedSupplier) {
            this.typeSecond = type;
            this.backgroundInfoSecond = backgroundInfo;
            this.buttonInfoSecond = buttonInfo;
            this.rawEntriesSecond = rawEntries;
            this.filtersMapSecond = filtersMap;
            this.selectedSupplierSecond = selectedSupplier;
            return this;
        }


        public Builder<T> searchBox(@Nullable WidgetInfoEditBox searchBoxInfo) {
            this.searchBoxInfo = searchBoxInfo;
            return this;
        }

        public Builder<T> checkboxAll(@Nullable WidgetInfoButtonImaged checkboxAllInfo) {
            this.checkboxAllInfo = checkboxAllInfo;
            return this;
        }




        public FilterListBinaryWidgetSet<T> build() {
            if(buttonInfoFirst == null){
                throw new IllegalArgumentException("first() filter widget set not specified");
            }
            if(buttonInfoSecond == null){
                throw new IllegalArgumentException("second() filter widget set not specified");
            }
            return new FilterListBinaryWidgetSet<>(this);
        }
    }
}

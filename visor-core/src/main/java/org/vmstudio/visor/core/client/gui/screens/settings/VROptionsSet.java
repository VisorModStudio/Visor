package org.vmstudio.visor.core.client.gui.screens.settings;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class VROptionsSet extends DynamicWidgetSet {
    @Getter
    private final VRSettingsScreen screen;

    @Getter
    private final @Nullable VROptionsSet previousOptions;

    private final Map<AbstractWidget, VROptionWidgetType> optionWidgets = new HashMap<>();



    public VROptionsSet(@NotNull VRSettingsScreen screen,
                        @Nullable VROptionsSet previousOptions,
                        @NotNull Runnable onWidgetsChanged){
        super(onWidgetsChanged);
        this.screen = screen;
        this.previousOptions = previousOptions;
    }

    protected abstract VROptionWidgetType[] getOptionTypes();

    protected abstract OptionWidgetEntry[] getOptionEntries();

    protected boolean canLoadDefaults(){
        return true;
    }
    protected void mouseClicked(double mouseX, double mouseY,
                                int button,
                                boolean success) {

    }

    public void reinit(){
        initWidgets();
        widgetsChanged();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        OptionWidgetEntry[] entries = getOptionEntries();
        if (entries != null && entries.length > 0) {
            initOptionEntries(entries, true);
        }

        VROptionWidgetType[] types = getOptionTypes();
        if (types == null || types.length < 1) {
            return getWidgets();
        }

        initOptionTypes(types, entries == null || entries.length < 1);

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        for(var widget : optionWidgets.keySet()){
            list.add((T)widget);
        }
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

    }

    @Override
    public void onTick() {

    }


    protected void initOptionEntries(OptionWidgetEntry[] entries,
                                     boolean clear) {
        if (clear) {
            optionWidgets.clear();
        }
        for (final OptionWidgetEntry entry : entries) {
            var widget = entry.createWidget();
            optionWidgets.put(widget, entry.getOptionType());
        }
    }
    protected void initOptionTypes(VROptionWidgetType[] options, boolean clear) {
        if (clear) {
            optionWidgets.clear();
        }

        ArrayList<OptionWidgetEntry> result = new ArrayList<>();

        int i = optionWidgets.size();
        for (VROptionWidgetType option : options) {
            OptionWidgetPosition optionPos = i % 2 == 0
                    ? OptionWidgetPosition.LEFT
                    : OptionWidgetPosition.RIGHT;

            if (option != VROptionWidgetType.EMPTY) {
                result.add(
                        new OptionWidgetEntry(
                                this,
                                option,
                                optionPos,
                                (int) Math.floor((float) i / 2.0F),
                                null
                        )
                );

            }
            i++;
        }

        this.initOptionEntries(
                result.toArray(new OptionWidgetEntry[0]), false
        );
    }


    public void previousOptions(){
        if(previousOptions != null){
            screen.switchOptions(previousOptions);
        }
    }
    public boolean canOpenPreviousPage(){
        return getPreviousOptions() != null;
    }

    public void loadDefaults(){
        for (var child : getWidgets()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            var optionType = getTypeFromWidget(widget);
            if(optionType == null) continue;
            ClientContext.settingsManager
                    .loadDefaultOptionValue(
                            optionType.getKey()
                    );
        }
        ClientContext.settingsManager.saveOptions();
        reinit();
    }

    public void switchOptions(@NotNull VROptionsSet newOptions){
        screen.switchOptions(newOptions);
    }

    public VROptionWidgetType getTypeFromWidget(AbstractWidget widget){
        return optionWidgets.get(widget);
    }
}

package me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.widgets.FilterListType;
import me.phoenixra.visor.api.client.gui.widgets.info.*;
import me.phoenixra.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.FilterListBinaryWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.SearchableListWidgetSet;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class OverlaysWidgetSet extends DynamicWidgetSet {


    @Getter
    private final VROverlaySettings owner;

    @Getter
    private SearchableListWidgetSet overlaysList;

    @Getter
    private SetupOverlayWidgetSet overlaySetup;

    private final Map<String, String> addonFiltersName = new LinkedHashMap<>();
    private final Map<String, Function<String, Boolean>> addonFiltersFunc = new LinkedHashMap<>();

    private final Map<String, String> mainFiltersName = new LinkedHashMap<>();
    private final Map<String, Function<String, Boolean>> mainFiltersFunc = new LinkedHashMap<>();


    @Getter @Setter
    private OverlayOptionGroup<?> copiedOptionGroup;

    public OverlaysWidgetSet(@NotNull VROverlaySettings owner,
                             @NotNull Runnable onWidgetsChanged) {
        super(onWidgetsChanged);
        this.owner = owner;

        //FILTER: CUSTOM ONLY
        String id = "custom_only";
        Component name = Component.translatable("visor.overlay.options.main.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return overlay.isCustom();
        });
        //FILTER: BUILT-IN ONLY
        id = "built_in_only";
        name = Component.translatable("visor.overlay.options.main.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return !overlay.isCustom();
        });
        //FILTER: Has options
        id = "has_options";
        name = Component.translatable("visor.overlay.options.main.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return !overlay.getOptions().isEmpty();
        });
        //FILTER: No options
        id = "no_options";
        name = Component.translatable("visor.overlay.options.main.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return overlay.getOptions().isEmpty();
        });

        //ADDON FILTERS
        for(var addon : VisorAPI.addonManager().getAddons()){
            String filterId = "addon_"+addon.getAddonId();
            addonFiltersName.put(
                    filterId,
                    addon.getAddonName().getString()
            );
            addonFiltersFunc.put(
                    filterId,
                    (it)->{
                        var overlay = ClientContext.overlayManager.getOverlay(it);
                        if(overlay == null){
                            return false;
                        }
                        return overlay.getOwner()
                                .getAddonId()
                                .equals(addon.getAddonId());
                    }
            );
        }

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        //clear cache
        overlaySetup = null;
        setOptionsMenu(null);


        List<String> filtersMain = new ArrayList<>();
        filtersMain.add("has_options");
        List<String> filtersAddons = new ArrayList<>(addonFiltersName.keySet().stream().toList());
        if(overlaysList != null){
            var binaryFilter = (FilterListBinaryWidgetSet<String>) overlaysList.getFilterWidgetSet();
            filtersMain.clear();
            filtersMain.addAll(
                    binaryFilter.getFiltersWidgetFirst()
                    .getActiveFilterIds()
            );
            filtersAddons.clear();
            filtersAddons.addAll(
                    binaryFilter.getFiltersWidgetSecond()
                    .getActiveFilterIds()
            );
        }

        Map<String, String> overlaysMap = new LinkedHashMap<>();
        ClientContext.overlayManager.getOverlaysRegistry()
                .getSortedByName().forEach(
                        it -> {
                            overlaysMap.put(it.getId(), it.getName().getString());
                        }
                );

        var filterBackgroundInfo = new WidgetInfoImage(
                SettingsTextures.FILTER_BACKGROUND,
                owner.getMenuEdgeX() - 117,
                owner.getMenuEdgeY() + 31,
                114, 220
        );
        var filterWidgetSet = new FilterListBinaryWidgetSet.Builder<String>(
                new WidgetInfoCheckboxList(
                        OverlayOptionTextures.BLACK_TEXTURE,
                        OverlayOptionTextures.CHECKBOX_BUTTON,
                        OverlayOptionTextures.CHECKBOX_BUTTON_HOVERED,
                        OverlayOptionTextures.CHECKBOX_BUTTON_SELECTED,
                        owner.getMenuEdgeX() - 117,
                        owner.getMenuEdgeY() + 76,
                        114, 167

                ).setTextureCheckboxHoveredSelected(OverlayOptionTextures.CHECKBOX_BUTTON_HOVERED_SELECTED)
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                onWidgetsChanged
        ).first( //Main
                FilterListType.ALL,
                filterBackgroundInfo,
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        owner.getMenuEdgeX() - 114,
                        owner.getMenuEdgeY() + 35,
                        52, 15
                ).setTextureSelected(OverlayOptionTextures.GENERAL_BUTTON_SELECTED)
                        .setText(Component.translatable("visor.overlay.options.main.overlays.filters.main"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                mainFiltersName,
                mainFiltersFunc,
                ()->filtersMain
        ).second( //addons
                FilterListType.AT_LEAST_ONE,
                filterBackgroundInfo,
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        owner.getMenuEdgeX() - 58,
                        owner.getMenuEdgeY() + 35,
                        52, 15
                ).setTextureSelected(OverlayOptionTextures.GENERAL_BUTTON_SELECTED)
                        .setText(Component.translatable("visor.overlay.options.main.overlays.filters.addons"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                addonFiltersName,
                addonFiltersFunc,
                ()->filtersAddons
        ).checkboxAll(
                new WidgetInfoButton(
                        SettingsTextures.CHECKBOX_ALL_BUTTON,
                        SettingsTextures.CHECKBOX_ALL_BUTTON_HOVERED,
                        owner.getMenuEdgeX() - 111,
                        owner.getMenuEdgeY() + 62,
                        12, 14
                ).setTextureSelected(SettingsTextures.CHECKBOX_ALL_BUTTON_SELECTED)
                        .setTextureHoveredSelected(SettingsTextures.CHECKBOX_ALL_BUTTON_HOVERED_SELECTED)
                        .setInactiveOnSelected(false)
        ).searchBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.BLACK_TEXTURE,
                        owner.getMenuEdgeX() - 95,
                        owner.getMenuEdgeY() + 62,
                        86,14
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();

        overlaysList = new SearchableListWidgetSet.Builder(
                new WidgetInfoSelectionList(
                        SettingsTextures.LIST_ENTRY,
                        SettingsTextures.LIST_ENTRY_HOVERED,
                        SettingsTextures.LIST_ENTRY_SELECTED,
                        owner.getMenuEdgeX() + 6,
                        owner.getMenuEdgeY() + 54,
                        111, 191
                ).setTextureScrollBarActive(OverlayOptionTextures.SCROLL_BAR_ACTIVE)
                        .setItemHeight(21)
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                overlaysMap,
                (selected) -> {
                    var overlay = ClientContext.overlayManager
                            .getOverlay(selected.getId());
                    if(overlay == null){
                        return;
                    }
                    createOverlaySetup(overlay);
                },
                onWidgetsChanged

        ).filterButton(
                new WidgetInfoButton(
                        SettingsTextures.FILTER_BLACK_BUTTON,
                        SettingsTextures.FILTER_BLACK_BUTTON_HOVERED,
                        owner.getMenuEdgeX() + 5,
                        owner.getMenuEdgeY() + 34,
                        17, 17
                ).setTextureSelected(SettingsTextures.FILTER_BLACK_BUTTON_SELECTED)
                        .setInactiveOnSelected(false),
                filterWidgetSet

        ).searchBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.BLACK_TEXTURE,
                        owner.getMenuEdgeX() + 25,
                        owner.getMenuEdgeY() + 35,
                        92, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();


        overlaysList.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>(overlaysList.getWidgets());
        if(overlaySetup != null){
            list.addAll(overlaySetup.getWidgets());
        }
        return list;
    }


    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        overlaysList.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        if(overlaysList.getFilterButton().isSelected()){
            owner.setCursorEdgeOffsetX(-owner.getMenuEdgeX());
            owner.setCursorEdgeOffsetY(-owner.getMenuEdgeY());
            owner.setCursorEdgeOffsetWidth(owner.getMenuEdgeX());
            owner.setCursorEdgeOffsetHeight(owner.getMenuEdgeY());
        }else{
            owner.setCursorEdgeOffsetX(0);
            owner.setCursorEdgeOffsetY(0);
            owner.setCursorEdgeOffsetWidth(0);
            owner.setCursorEdgeOffsetHeight(0);
        }
        if(overlaySetup != null){
            overlaySetup.onPreRender(
                    guiGraphics,
                    mouseX, mouseY,
                    partialTicks
            );
        }
    }

    @Override
    public void onTick() {
        overlaysList.onTick();
        if(overlaySetup != null){
            overlaySetup.onTick();
        }
    }

    public void setOptionsMenu(@Nullable OverlayOptionGroup menu){
        VROverlayOptionsMenu optionsMenu = ClientContext.overlayManager
                .getOverlay(VROverlayOptionsMenu.ID, VROverlayOptionsMenu.class);
        assert optionsMenu != null;

        if(menu != null){
            if(menu == optionsMenu.getOptionsGroup()){
                optionsMenu.init();
                return;
            }
            optionsMenu.openMenu(
                    owner,
                    menu
            );
            return;
        }
        optionsMenu.setEnabled(false);
    }
    public void removeOverlay(@NotNull VROverlay overlay){
        if(!overlay.isCustom()){
            throw new IllegalArgumentException("Not allowed to remove built-in overlays in settings");
        }
        ClientContext.overlayManager.getOverlaysRegistry()
                .unregisterElement(overlay.getId());
        initWidgets();
        widgetsChanged();
    }


    private void createOverlaySetup(VROverlay overlay){
        setOptionsMenu(null);

        overlaySetup = new SetupOverlayWidgetSet(
                this,
                overlay,
                onWidgetsChanged,
                owner.getMenuEdgeX() + 122,
                owner.getMenuEdgeY() + 35
        );
        overlaySetup.initWidgets();
        widgetsChanged();
    }
}

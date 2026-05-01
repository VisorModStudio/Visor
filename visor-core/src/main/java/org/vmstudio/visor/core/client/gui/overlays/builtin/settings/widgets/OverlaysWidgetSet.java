package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.widgets.info.*;
import org.vmstudio.visor.api.client.gui.widgets.lists.FilterListType;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.sets.FilterListBinaryWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.sets.SearchableListWidgetSet;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
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



    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        //FILTER: CUSTOM ONLY
        String id = "custom_only";
        Component name = Component.translatable("visor.overlay.options.overlays.filters.main."+id);
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
        name = Component.translatable("visor.overlay.options.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return overlay.isBuiltIn();
        });
        //FILTER: Has options
        id = "has_options";
        name = Component.translatable("visor.overlay.options.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return overlay.hasModifiableOptions();
        });
        //FILTER: No options
        id = "no_options";
        name = Component.translatable("visor.overlay.options.overlays.filters.main."+id);
        mainFiltersName.put(id, name.getString());
        mainFiltersFunc.put(id, (it)->{
            var overlay = ClientContext.overlayManager.getOverlay(it);
            if(overlay == null){
                return false;
            }
            return !overlay.hasModifiableOptions();
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

        var filterBackgroundInfo = new WidgetInfoImage()
                .pos(owner.getMenuBoundsX() - 117, owner.getMenuBoundsY() + 31)
                .size(114, 220)
                .setTexture(SettingsTextures.FILTER_BACKGROUND);
        var filterWidgetSet = new FilterListBinaryWidgetSet.Builder<String>(
                new WidgetInfoCheckboxList()
                        .pos(owner.getMenuBoundsX() - 117,owner.getMenuBoundsY() + 76)
                        .size(114, 167)
                        .textures(
                                OptionTextures.BLACK_TEXTURE,
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                onWidgetsChanged
        ).first( //Main
                FilterListType.ALL,
                filterBackgroundInfo,
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() - 114, owner.getMenuBoundsY() + 35)
                        .size(52, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setText(Component.translatable("visor.overlay.options.overlays.filters.main"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                mainFiltersName,
                mainFiltersFunc,
                ()->filtersMain
        ).second( //addons
                FilterListType.AT_LEAST_ONE,
                filterBackgroundInfo,
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() - 58,owner.getMenuBoundsY() + 35)
                        .size(52, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setText(Component.translatable("visor.overlay.options.overlays.filters.addons"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                addonFiltersName,
                addonFiltersFunc,
                ()->filtersAddons
        ).checkboxAll(
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() - 111,owner.getMenuBoundsY() + 62)
                        .size(12, 14)
                        .textures(
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED,
                                null
                        )
                        .setInactiveOnSelected(false)
        ).searchBox(
                new WidgetInfoEditBox()
                        .pos(owner.getMenuBoundsX() - 95,owner.getMenuBoundsY() + 62)
                        .size(86,14)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();

        overlaysList = new SearchableListWidgetSet.Builder(
                new WidgetInfoSelectionList()
                        .pos(owner.getMenuBoundsX() + 6,owner.getMenuBoundsY() + 54)
                        .size(111, 191)
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE)
                        .setEntryHeight(21)
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
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() + 5,owner.getMenuBoundsY() + 34)
                        .size(17, 17)
                        .textures(
                                SettingsTextures.FILTER_BLACK_BUTTON,
                                SettingsTextures.FILTER_BLACK_BUTTON_HOVERED,
                                SettingsTextures.FILTER_BLACK_BUTTON_SELECTED
                        )
                        .setInactiveOnSelected(false),
                filterWidgetSet

        ).searchBox(
                new WidgetInfoEditBox()
                        .pos(owner.getMenuBoundsX() + 25,owner.getMenuBoundsY() + 35)
                        .size(92, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
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
            owner.setCursorBoundsOffsetX(-owner.getMenuBoundsX());
            owner.setCursorBoundsOffsetY(-owner.getMenuBoundsY());
            owner.setCursorBoundsOffsetWidth(owner.getMenuBoundsX());
            owner.setCursorBoundsOffsetHeight(owner.getMenuBoundsY());
        }else{
            owner.setCursorBoundsOffsetX(0);
            owner.setCursorBoundsOffsetY(0);
            owner.setCursorBoundsOffsetWidth(0);
            owner.setCursorBoundsOffsetHeight(0);
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

        //silly way to auto rename overlay entry when identity is modified
        var selected = overlaysList.getList().getSelectedEntry();
        if(selected == null){
            return;
        }
        var selectedOverlay = ClientContext.overlayManager.getOverlay(selected.getId());
        if(selectedOverlay == null){
            return;
        }
        overlaysList.getList().renameEntry(
                selected.getId(), selectedOverlay.getName()
        );

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
        if(overlay.isBuiltIn()){
            throw new IllegalArgumentException("Not allowed to remove built-in overlays in settings");
        }
        ClientContext.overlayManager.getOverlaysRegistry()
                .unregisterComponent(overlay.getId());
        initWidgets();
        widgetsChanged();
    }


    private void createOverlaySetup(VROverlay overlay){
        setOptionsMenu(null);

        overlaySetup = new SetupOverlayWidgetSet(
                this,
                overlay,
                onWidgetsChanged,
                owner.getMenuBoundsX() + 122,
                owner.getMenuBoundsY() + 35
        );
        overlaySetup.initWidgets();
        widgetsChanged();
    }
}

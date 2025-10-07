package me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplateRecord;
import me.phoenixra.visor.api.client.gui.overlay.options.types.OverlayOptionsIdentity;
import me.phoenixra.visor.api.client.gui.widgets.FilterListType;
import me.phoenixra.visor.api.client.gui.widgets.ImageButton;
import me.phoenixra.visor.api.client.gui.widgets.TextBoxEditable;
import me.phoenixra.visor.api.client.gui.widgets.TexturedEditBox;
import me.phoenixra.visor.api.client.gui.widgets.info.*;
import me.phoenixra.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.FiltersListWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.SearchableListWidgetSet;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import me.phoenixra.visor.core.client.gui.registry.VROverlayRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class CreateOverlayWidgetSet extends DynamicWidgetSet {
    @Getter
    private final VROverlaySettings owner;


    private TexturedEditBox idWidget;
    private TexturedEditBox nameWidget;
    private TextBoxEditable descriptionWidget;

    private SetupIconWidgetSet setupIconWidget;

    private SearchableListWidgetSet templatesWidget;

    private ImageButton createButton;

    private final Map<String, String> addonFiltersName = new LinkedHashMap<>();
    private final Map<String, Function<String, Boolean>> addonFiltersFunc = new LinkedHashMap<>();

    private int filterStartX;
    private int filterStartY;


    private VROverlayTemplateRecord selectedTemplate = null;
    public CreateOverlayWidgetSet(@NotNull VROverlaySettings owner,
                                  @NotNull Runnable onWidgetsChanged){
        super(onWidgetsChanged);
        this.owner = owner;

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

        List<String> filtersAddons = new ArrayList<>(addonFiltersName.keySet().stream().toList());
        if(templatesWidget != null){
            var binaryFilter = (FiltersListWidgetSet<String>)templatesWidget.getFilterWidgetSet();
            filtersAddons.clear();
            filtersAddons.addAll(
                    binaryFilter.getActiveFilterIds()
            );
        }

        Map<String, String> templatesMap = new LinkedHashMap<>();
        ClientContext.overlayManager.getOverlayTemplatesRegistry()
                .getAllElements().forEach(
                        it -> {
                            templatesMap.put(it.getId(), it.name().getString());
                        }
                );

        idWidget = new TexturedEditBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.DARK_GRAY_TEXTURE,
                        owner.getMenuEdgeX() + 14,
                        owner.getMenuEdgeY() + 43,
                        92, 13
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.main.create_overlay.type_id"))
        );
        nameWidget = new TexturedEditBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.DARK_GRAY_TEXTURE,
                        owner.getMenuEdgeX() + 14,
                        owner.getMenuEdgeY() + 66,
                        92, 13
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.main.create_overlay.type_name"))
        );
        descriptionWidget = new TextBoxEditable(
                new WidgetInfoTextBoxEditable(
                        owner.getMenuEdgeX() + 14,
                        owner.getMenuEdgeY() + 89,
                        92, 54
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTextHintColor(VROverlaySettings.TEXT_COLOR)
                        .setTextScale(0.6f)
                        .setBackground(OverlayOptionTextures.DARK_GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.main.create_overlay.type_description"))
        );

        setupIconWidget = new SetupIconWidgetSet(
                this,
                owner.getMenuEdgeX() + 6,
                owner.getMenuEdgeY() + 151
        );

        createButton = new ImageButton(
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        owner.getMenuEdgeX() + 122,
                        owner.getMenuEdgeY() + 230,
                        102, 15
                ).setTextureInactive(SettingsTextures.CREATE_BUTTON_WARNING)
                        .setText(Component.translatable("visor.overlay.options.main.create_overlay.create"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                (button)->{
                    if(isReadyToCreate() != null) return;
                    create();
                }
        );

        filterStartX = owner.getMenuEdgeX() + 234;
        filterStartY = owner.getMenuEdgeY() + 57;
        var filterWidget = new FiltersListWidgetSet.Builder<>(
                FilterListType.AT_LEAST_ONE,
                new WidgetInfoCheckboxList(
                        OverlayOptionTextures.BLACK_TEXTURE,
                        OverlayOptionTextures.CHECKBOX_BUTTON,
                        OverlayOptionTextures.CHECKBOX_BUTTON_HOVERED,
                        OverlayOptionTextures.CHECKBOX_BUTTON_SELECTED,
                        filterStartX + 7,
                        filterStartY + 51,
                        103, 133

                ).setTextureCheckboxHoveredSelected(OverlayOptionTextures.CHECKBOX_BUTTON_HOVERED_SELECTED)
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                addonFiltersName,
                addonFiltersFunc,
                () -> filtersAddons
        ).background(
                 new WidgetInfoImage(
                        SettingsTextures.FILTER_BACKGROUND,
                         filterStartX,
                         filterStartY,
                        114, 188
                )
        ).checkboxAll(
                new WidgetInfoButton(
                        SettingsTextures.CHECKBOX_ALL_BUTTON,
                        SettingsTextures.CHECKBOX_ALL_BUTTON_HOVERED,
                        filterStartX + 6,
                        filterStartY + 33,
                        12, 14
                ).setTextureSelected(SettingsTextures.CHECKBOX_ALL_BUTTON_SELECTED)
                        .setTextureHoveredSelected(SettingsTextures.CHECKBOX_ALL_BUTTON_HOVERED_SELECTED)
                        .setInactiveOnSelected(false)
        ).searchBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.BLACK_TEXTURE,
                        filterStartX + 22,
                        filterStartY + 33,
                        86,14
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();
        templatesWidget = new SearchableListWidgetSet.Builder(
                new WidgetInfoSelectionList(SettingsTextures.LIST_ENTRY,
                        SettingsTextures.LIST_ENTRY_HOVERED,
                        SettingsTextures.LIST_ENTRY_SELECTED,
                        owner.getMenuEdgeX() + 125,
                        owner.getMenuEdgeY() + 68,
                        96, 154
                ).setTooltip((id)->{
                    var registry = ClientContext.overlayManager.getOverlayTemplatesRegistry();
                    var template = registry.getElement(id);
                    if(template == null) return Component.empty();
                    return Component.translatable(
                            "visor.overlay.options.main.create_overlay.template_tooltip",
                            template.getOwner().getAddonName(),
                            template.id(),
                            template.description()
                    );
                }),
                templatesMap,
                (selected)->{
                    var registry = ClientContext.overlayManager.getOverlayTemplatesRegistry();
                    selectedTemplate = registry.getElement(selected.getId());
                },
                onWidgetsChanged

        ).searchBox(
                new WidgetInfoEditBox(
                        OverlayOptionTextures.DARK_GRAY_TEXTURE,
                        owner.getMenuEdgeX() + 125,
                        owner.getMenuEdgeY() + 50,
                        77, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).filterButton(
                new WidgetInfoButton(
                        SettingsTextures.FILTER_GRAY_BUTTON,
                        SettingsTextures.FILTER_GRAY_BUTTON_HOVERED,
                        owner.getMenuEdgeX() + 206,
                        owner.getMenuEdgeY() + 50,
                        15, 15
                ).setTextureSelected(SettingsTextures.FILTER_GRAY_BUTTON_SELECTED)
                        .setInactiveOnSelected(false),
                filterWidget
        ).build();

        setupIconWidget.initWidgets();
        templatesWidget.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) idWidget);
        list.add((T) nameWidget);
        list.add((T) descriptionWidget);
        list.addAll(setupIconWidget.getWidgets());
        list.addAll(templatesWidget.getWidgets());
        list.add((T)createButton);
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int textColor = VROverlaySettings.TEXT_COLOR.toInt();

        boolean filterSelected =  templatesWidget.getFilterButton()
                .isSelected();
        Component readyFallback = isReadyToCreate();

        createButton.active = readyFallback == null;
        if(readyFallback != null) {
            createButton.setTooltip(Tooltip.create(readyFallback));
        }else{
            createButton.setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.main.create_overlay.create.tooltip")));
        }

        owner.setBackgroundExtended(
                filterSelected
        );
        setupIconWidget.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        templatesWidget.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);

        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                Component.translatable("visor.overlay.options.main.create_overlay.select_template").getString(),
                textColor,
                getOwner().getMenuEdgeX() + 129,
                getOwner().getMenuEdgeY() + 38,
                88, 8,
                true
        );

        if(filterSelected) {
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.main.overlays.filters.addons").getString(),
                    textColor,
                    filterStartX + 8,
                    filterStartY + 6,
                    98, 11,
                    true
            );
        }
    }

    @Override
    public void onTick() {
        descriptionWidget.tick();
        setupIconWidget.onTick();
        templatesWidget.onTick();
    }


    private Component isReadyToCreate(){
        if(idWidget.getValue().isBlank()){
            return Component.translatable("visor.overlay.options.main.create_overlay.create.tooltip.id");
        }
        VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
        if(registry.getElement(idWidget.getValue()) != null) {
            return Component.translatable("visor.overlay.options.main.create_overlay.create.tooltip.id.exists");
        }

        if(nameWidget.getValue().isBlank()){
            return Component.translatable("visor.overlay.options.main.create_overlay.create.tooltip.name");
        }

        if(selectedTemplate == null){
            return Component.translatable("visor.overlay.options.main.create_overlay.create.tooltip.template");
        }

        return null;
    }

    private void create(){
        String id = idWidget.getValue();
        String name = nameWidget.getValue();
        String description = descriptionWidget.getValue();
        if(description.isBlank()){
            description = null;
        }

        VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
        try {
            VROverlayTemplate overlay = selectedTemplate.constructor().newInstance(
                    ClientContext.coreAddon,
                    id
            );
            registry.registerElement(overlay);

            //apply identity
            var identity = overlay.getOption(OverlayOptionsIdentity.ID, OverlayOptionsIdentity.class);
            Objects.requireNonNull(identity);

            identity.setName(name);
            identity.setDescription(description);

            var iconResource = setupIconWidget.getIcon().getResourceLocation();
            String iconPath = iconResource.getNamespace() + ":" + iconResource.getPath();
            identity.setIcon(iconPath);

            identity.save();

            //finish
            overlay.updateIdentity();
            owner.setOverlaysTab(overlay);

        }catch (Exception e){
            VisorState.destroyVRWithErrorScreen(e);
        }
    }
}

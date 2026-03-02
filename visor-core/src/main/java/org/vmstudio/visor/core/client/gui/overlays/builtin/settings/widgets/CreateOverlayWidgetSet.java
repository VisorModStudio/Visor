package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplateRecord;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import org.vmstudio.visor.api.client.gui.widgets.info.*;
import org.vmstudio.visor.api.client.gui.widgets.lists.FilterListType;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.sets.FiltersListWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.sets.SearchableListWidgetSet;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets.identity.SetupIdentityWidgetSet;
import org.vmstudio.visor.core.client.gui.registry.VROverlayRegistry;
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

    private SetupIdentityWidgetSet setupIdentity;

    private SearchableListWidgetSet templatesWidget;

    private ButtonImaged createButton;

    private final Map<String, String> addonFiltersName = new LinkedHashMap<>();
    private final Map<String, Function<String, Boolean>> addonFiltersFunc = new LinkedHashMap<>();

    private int filterStartX;
    private int filterStartY;


    private VROverlayTemplateRecord selectedTemplate = null;
    public CreateOverlayWidgetSet(@NotNull VROverlaySettings owner,
                                  @NotNull Runnable onWidgetsChanged){
        super(onWidgetsChanged);
        this.owner = owner;

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        for(var addon : VisorAPI.addonManager().getAddons()){
            String filterId = "addon_"+addon.getAddonId();
            addonFiltersName.put(
                    filterId,
                    addon.getAddonName().getString()
            );
            addonFiltersFunc.put(
                    filterId,
                    (it)->{
                        var templateRecord = ClientContext.overlayManager.getOverlayTemplatesRegistry().getComponent(it);
                        if(templateRecord == null){
                            return false;
                        }
                        return templateRecord.getOwner()
                                .getAddonId()
                                .equals(addon.getAddonId());
                    }
            );
        }
        List<String> filtersAddons = new ArrayList<>(addonFiltersName.keySet().stream().toList());
        if(templatesWidget != null){
            var filter = (FiltersListWidgetSet<String>)templatesWidget.getFilterWidgetSet();
            filtersAddons.clear();
            filtersAddons.addAll(
                    filter.getActiveFilterIds()
            );
        }

        Map<String, String> templatesMap = new LinkedHashMap<>();
        ClientContext.overlayManager.getOverlayTemplatesRegistry()
                .getAllComponents().forEach(
                        it -> {
                            templatesMap.put(it.getId(), it.name().getString());
                        }
                );

        setupIdentity = new SetupIdentityWidgetSet(
                owner.getMenuBoundsX(), owner.getMenuBoundsY(),
                true
        );

        createButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() + 122, owner.getMenuBoundsY() + 230)
                        .size(102, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setTextureInactive(SettingsTextures.CREATE_BUTTON_WARNING)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT,
                                0.65f
                        )
                        .setText(Component.translatable("visor.overlay.options.overlays.create_overlay.create"))
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                (button)->{
                    if(isReadyToCreate() != null) return;
                    create();
                }
        );

        filterStartX = owner.getMenuBoundsX() + 234;
        filterStartY = owner.getMenuBoundsY() + 57;
        var filterWidget = new FiltersListWidgetSet.Builder<>(
                FilterListType.AT_LEAST_ONE,
                new WidgetInfoCheckboxList()
                        .pos(filterStartX + 7,filterStartY + 51)
                        .size(103, 133)
                        .textures(
                                OptionTextures.BLACK_TEXTURE,
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR),
                addonFiltersName,
                addonFiltersFunc,
                () -> filtersAddons
        ).background(
                 new WidgetInfoImage()
                         .pos(filterStartX, filterStartY)
                         .size(114, 188)
                         .setTexture(SettingsTextures.FILTER_BACKGROUND)
        ).checkboxAll(
                new WidgetInfoButtonImaged()
                        .pos(filterStartX + 6, filterStartY + 33)
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
                        .pos(filterStartX + 22, filterStartY + 33)
                        .size(86,14)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();
        templatesWidget = new SearchableListWidgetSet.Builder(
                new WidgetInfoSelectionList()
                        .pos(owner.getMenuBoundsX() + 125, owner.getMenuBoundsY() + 68)
                        .size(96, 154)
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTooltip((id)->{
                    var registry = ClientContext.overlayManager.getOverlayTemplatesRegistry();
                    var template = registry.getComponent(id);
                    if(template == null) return Component.empty();
                    return Component.translatable(
                            "visor.overlay.options.overlays.create_overlay.template_tooltip",
                            template.getOwner().getAddonName(),
                            template.id(),
                            template.description()
                    );
                }),
                templatesMap,
                (selected)->{
                    var registry = ClientContext.overlayManager.getOverlayTemplatesRegistry();
                    selectedTemplate = registry.getComponent(selected.getId());
                },
                onWidgetsChanged

        ).searchBox(
                new WidgetInfoEditBox()
                        .pos(owner.getMenuBoundsX() + 125, owner.getMenuBoundsY() + 50)
                        .size(77, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).filterButton(
                new WidgetInfoButtonImaged()
                        .pos(owner.getMenuBoundsX() + 206, owner.getMenuBoundsY() + 50)
                        .size(15, 15)
                        .textures(
                                SettingsTextures.FILTER_GRAY_BUTTON,
                                SettingsTextures.FILTER_GRAY_BUTTON_HOVERED,
                                SettingsTextures.FILTER_GRAY_BUTTON_SELECTED
                        )
                        .setInactiveOnSelected(false),
                filterWidget
        ).build();

        setupIdentity.initWidgets();
        templatesWidget.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.addAll(setupIdentity.getWidgets());
        list.addAll(templatesWidget.getWidgets());
        list.add((T)createButton);
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int textColor = VROverlaySettings.TEXT_COLOR.asInt();

        boolean filterSelected =  templatesWidget.getFilterButton()
                .isSelected();
        Component readyFallback = isReadyToCreate();

        createButton.active = readyFallback == null;
        if(readyFallback != null) {
            createButton.setTooltip(Tooltip.create(readyFallback));
        }else{
            createButton.setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip")));
        }

        owner.setBackgroundExtended(
                filterSelected
        );
        setupIdentity.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
        templatesWidget.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);

        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                Component.translatable("visor.overlay.options.overlays.create_overlay.select_template").getString(),
                textColor,
                getOwner().getMenuBoundsX() + 129,
                getOwner().getMenuBoundsY() + 38,
                88, 8,
                true
        );

        if(filterSelected) {
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.overlays.filters.addons").getString(),
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
        setupIdentity.onTick();
        templatesWidget.onTick();
    }


    private Component isReadyToCreate(){
        String id = setupIdentity.getIdWidget().getValue();
        if(id.isBlank()){
            return Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip.id");
        }
        if(!ComponentIds.isValid(id)){
            return Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip.pattern");
        }
        VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
        if(registry.getComponent(id) != null) {
            return Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip.exists");
        }

        if(setupIdentity.getNameWidget().getValue().isBlank()){
            return Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip.name");
        }

        if(selectedTemplate == null){
            return Component.translatable("visor.overlay.options.overlays.create_overlay.create.tooltip.template");
        }

        return null;
    }

    private void create(){
        String id = setupIdentity.getIdWidget().getValue();
        String name = setupIdentity.getNameWidget().getValue();
        String description = setupIdentity.getDescriptionWidget().getValue();
        if(description.isBlank()){
            description = null;
        }

        VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
        try {
            VROverlayTemplate overlay = selectedTemplate.constructor().newInstance(
                    ClientContext.coreAddon,
                    id
            );
            registry.registerComponent(overlay);

            //apply identity
            var identity = overlay.getOption(OverlayOptionsIdentity.ID, OverlayOptionsIdentity.class);
            Objects.requireNonNull(identity);

            identity.setName(name);
            identity.setDescription(description);

            var iconResource = setupIdentity.getSetupIconWidget().getIcon().getResourceLocation();
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

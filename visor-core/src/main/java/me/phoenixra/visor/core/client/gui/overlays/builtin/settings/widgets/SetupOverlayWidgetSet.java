package me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets;

import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.widgets.ImageButton;
import me.phoenixra.visor.api.client.gui.widgets.TextBoxEditable;
import me.phoenixra.visor.api.client.gui.widgets.TexturedSelectionList;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButton;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;
import me.phoenixra.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
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

public class SetupOverlayWidgetSet extends DynamicWidgetSet {
    private final OverlaysWidgetSet owner;

    private final VROverlay overlay;

    private final int startX;
    private final int startY;

    private final boolean hasSettings;
    private final boolean isCustom;
    private final VROverlayTemplate asTemplate;

    private TextBoxEditable descriptionWidget;

    private TexturedSelectionList optionsListWidget;
    private ImageButton loadDefaultsWidget;
    private ImageButton saveButtonWidget;
    private ImageButton removeButtonWidget;

    private boolean confirmRemove;
    private ImageButton confirmRemoveWidget;
    private ImageButton cancelRemoveWidget;


    private ImageButton copyButtonWidget;
    private ImageButton pasteButtonWidget;
    private ImageButton loadFromFileButtonWidget;

    private HashMap<String, OverlayOptionGroup<?>> optionsMap;


    public SetupOverlayWidgetSet(@NotNull OverlaysWidgetSet owner,
                                 @NotNull VROverlay overlay,
                                 @NotNull Runnable onWidgetsChanged,
                                 int startX, int startY) {
        super(onWidgetsChanged);
        this.owner = owner;
        this.overlay = overlay;
        this.startX = startX;
        this.startY = startY;
        this.hasSettings = !overlay.getOptions().isEmpty();
        this.isCustom = overlay.isCustom();
        this.asTemplate = overlay.asTemplate();

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        descriptionWidget = new TextBoxEditable(
                new WidgetInfoTextBoxEditable(
                        startX + 5,
                        startY + 39,
                        92, 54
                ).setText(overlay.getDescription())
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTextScale(0.6f)
                        .setBackground(OverlayOptionTextures.DARK_GRAY_TEXTURE)

        );
        descriptionWidget.setEditable(false);

        if (!hasSettings) {
            return getWidgets();
        }
        copyButtonWidget = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.COPY_BUTTON,
                        SettingsTextures.COPY_BUTTON_HOVERED,
                        startX + 112,
                        startY + 99,
                        17, 17
                ).setTextureInactive(SettingsTextures.COPY_BUTTON_INACTIVE)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.main.overlays.copy_options.tooltip"))),
                (button)->{
                    var selectedEntry = optionsListWidget.getSelected();
                    OverlayOptionGroup<?> options = optionsMap.get(selectedEntry.getId());
                    if (options == null) return;
                    if(!options.supportsCopying()) return;
                    owner.setCopiedOptionGroup(options);
                    widgetsChanged();
                }
        );
        pasteButtonWidget = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.PASTE_BUTTON,
                        SettingsTextures.PASTE_BUTTON_HOVERED,
                        startX + 112,
                        startY + 124,
                        17, 17
                ).setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.main.overlays.paste_options.tooltip")))
                        .setTextureInactive(SettingsTextures.PASTE_BUTTON_INACTIVE),
                (button)->{
                    var optionsToCopy = owner.getCopiedOptionGroup();
                    var selectedEntry = optionsListWidget.getSelected();
                    OverlayOptionGroup<?> optionsTarget = optionsMap.get(selectedEntry.getId());
                    if(optionsToCopy == null){
                        return;
                    }
                    if(optionsTarget == null
                            || !optionsTarget.canCopyFrom(optionsToCopy)) {
                        return;
                    }

                    optionsTarget.loadFromOther(optionsToCopy);
                    owner.setCopiedOptionGroup(null);
                    widgetsChanged();
                }
        );

        loadFromFileButtonWidget = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.BUTTON_LOAD,
                        SettingsTextures.BUTTON_LOAD_HOVERED,
                        startX + 112,
                        startY + 156,
                        17, 17
                ).setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.main.overlays.load.tooltip"))),
                button -> {
                    overlay.reloadOptions();
                }
        );
        var rawEntries = new LinkedHashMap<String, String>();
        optionsMap = new HashMap<>();
        for (var entry : overlay.getOptions()) {
            rawEntries.put(entry.getId(), entry.getDisplayName().getString());
            optionsMap.put(entry.getId(), entry);
        }
        optionsListWidget = new TexturedSelectionList(
                new WidgetInfoSelectionList(
                        SettingsTextures.LIST_ENTRY,
                        SettingsTextures.LIST_ENTRY_HOVERED,
                        SettingsTextures.LIST_ENTRY_SELECTED,
                        startX + 5, startY + 110,
                        93, 58
                ).setTextureScrollBarActive(OverlayOptionTextures.SCROLL_BAR_ACTIVE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setSupportDeselection(true),
                rawEntries,
                it -> {
                    if(it == null){
                        owner.setOptionsMenu(null);
                        return;
                    }
                    OverlayOptionGroup<?> options = optionsMap.get(it.getId());
                    if (options == null) return;
                    owner.setOptionsMenu(options);
                }
        );

        loadDefaultsWidget = new ImageButton(
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        startX,
                        startY + 176,
                        102, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.main.overlays.load_defaults")),
                (it) -> {
                    loadDefaults();
                }
        );
        saveButtonWidget = new ImageButton(
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        this.isCustom ? startX : startX + 19,
                        startY + 195,
                        83, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.main.overlays.save")),
                (it) -> {
                    saveChanges();
                }
        );
        if (!isCustom) {
            return getWidgets();
        }

        //CUSTOM ONLY
        removeButtonWidget = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.REMOVE_BUTTON,
                        SettingsTextures.REMOVE_BUTTON_HOVERED,
                        startX + 87,
                        startY + 195,
                        15, 15
                ),
                (it) -> {
                    confirmRemove = true;
                    widgetsChanged();
                }
        );

        confirmRemoveWidget = new ImageButton(
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                        startX,
                        startY + 195,
                        83, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.main.overlays.confirm_remove")),
                (it) -> {
                    confirmRemove = false;
                    owner.removeOverlay(overlay);
                }
        );
        cancelRemoveWidget = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.CANCEL_BUTTON,
                        SettingsTextures.CANCEL_BUTTON_HOVERED,
                        startX + 87,
                        startY + 195,
                        15, 15
                ),
                (it) -> {
                    confirmRemove = false;
                    widgetsChanged();
                }
        );
        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) descriptionWidget);
        if(!hasSettings){
            return list;
        }

        list.add((T)copyButtonWidget);
        if(owner.getCopiedOptionGroup() != null){
            list.add((T)pasteButtonWidget);
        }
        list.add((T)loadFromFileButtonWidget);

        list.add((T) optionsListWidget);
        list.add((T) loadDefaultsWidget);
        if(isCustom){
            if(confirmRemove){
                list.add((T) confirmRemoveWidget);
                list.add((T) cancelRemoveWidget);
            }else {
                list.add((T) removeButtonWidget);
                list.add((T) saveButtonWidget);
            }
        }
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //PREPARE WIDGETS
        if(hasSettings){
            var selectedEntry = optionsListWidget.getSelected();
            OverlayOptionGroup<?> options = null;
            if(selectedEntry != null){
                options = optionsMap.get(selectedEntry.getId());;
            }

            copyButtonWidget.visible = options != null;
            copyButtonWidget.active = options != null
                    && options.supportsCopying();

            if(owner.getCopiedOptionGroup() != null) {
                pasteButtonWidget.active = options != null
                        && options.canCopyFrom(owner.getCopiedOptionGroup());
            }
            if(isCustom && !confirmRemove){
                boolean changesNotSaved = false;
                for(var optionGroup : optionsMap.values()){
                    if(optionGroup.isChangesNotSaved()){
                        changesNotSaved = true;
                        break;
                    }
                }
                var widgetInfo = saveButtonWidget.getWidgetInfo();
                if(changesNotSaved) {
                    widgetInfo.getTextPosOffset().x = 13;
                    widgetInfo.getTextScaleOffset().x = -26;
                    widgetInfo.setTexture(
                            SettingsTextures.BUTTON_SAVE_WARNING
                    );
                    widgetInfo.setTextureHovered(
                            SettingsTextures.BUTTON_SAVE_WARNING_HOVERED
                    );
                }else{
                    widgetInfo.getTextScaleOffset().x = 0;
                    widgetInfo.getTextPosOffset().x = 0;
                    widgetInfo.setTexture(
                            OverlayOptionTextures.GENERAL_BUTTON
                    );
                    widgetInfo.setTextureHovered(
                            OverlayOptionTextures.GENERAL_BUTTON_HOVERED
                    );
                }
            }
        }

        //RENDERING
        Font font = Minecraft.getInstance().font;
        int textColor = VROverlaySettings.TEXT_COLOR.toInt();
        var icon = overlay.getIcon();
        GuiTexture labelTexture =
                this.isCustom
                        ? SettingsTextures.LABEL_CUSTOM
                        : SettingsTextures.LABEL_BUILT_IN;
        Component addonText = Component.translatable("visor.overlay.options.main.overlays.addon", overlay.getOwner().getAddonName());
        Component idText = Component.translatable("visor.overlay.options.main.overlays.id", overlay.getId());

        //Overlay icon
        icon.blit(
                guiGraphics,
                startX + 5, startY + 3,
                19, 19
        );
        //Overlay name
        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                overlay.getName().getString(),
                textColor,
                startX + 33,
                startY + 4,
                66, 6,
                true
        );
        //Overlay addon name
        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                addonText.getString(),
                textColor,
                startX + 33,
                startY + 13,
                66, 5,
                false
        );
        //Overlay ID
        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                idText.getString(),
                textColor,
                startX + 33,
                startY + 19,
                66, 5,
                false
        );

        //Overlay label
        labelTexture.blit(
                guiGraphics,
                startX + 5,
                startY + 25,
                10, 10
        );
        //Overlay template
        if (this.isCustom) {
            Component templateText = Component.translatable("visor.overlay.options.main.overlays.template", asTemplate.getTemplateName().getString());

            OverlayOptionTextures.DARK_GRAY_TEXTURE.blit(
                    guiGraphics,
                    startX + 24,
                    startY + 25,
                    75, 10
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    templateText.getString(),
                    textColor,
                    startX + 24 + 2,
                    startY + 25 + 2,
                    75 - 2, 10 - 2,
                    false
            );
        }
        //Settings background
        if (!hasSettings) {
            OverlayOptionTextures.BLACK_TEXTURE.blit(
                    guiGraphics,
                    startX,
                    startY + 99,
                    102, 111
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.main.overlays.options_not_found")
                            .getString(),
                    textColor,
                    startX + 15,
                    startY + 152,
                    72, 8,
                    true
            );
        } else {
            OverlayOptionTextures.BLACK_TEXTURE.blit(
                    guiGraphics,
                    startX,
                    startY + 99,
                    102, 73
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.main.overlays.select_options")
                            .getString(),
                    textColor,
                    startX + 9,
                    startY + 102,
                    84, 8,
                    true
            );
        }
    }

    @Override
    public void onTick() {

    }

    private void loadDefaults(){
        var entry = optionsListWidget.getSelected();
        if(entry == null){
            for(var options : optionsMap.values()){
                options.loadDefaults();
            }
            return;
        }
        OverlayOptionGroup<?> options = optionsMap.get(entry.getId());
        if (options == null) return;

        options.loadDefaults();
        owner.setOptionsMenu(options);
    }


    private void saveChanges(){
        for(var options : optionsMap.values()){
            options.save();
        }
    }



}

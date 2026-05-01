package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets;

import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.TextBoxEditable;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
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
    private ButtonImaged loadDefaultsWidget;
    private ButtonImaged saveButtonWidget;
    private ButtonImaged removeButtonWidget;

    private boolean confirmRemove;
    private ButtonImaged confirmRemoveWidget;
    private ButtonImaged cancelRemoveWidget;


    private ButtonImaged copyButtonWidget;
    private ButtonImaged pasteButtonWidget;
    private ButtonImaged loadFromFileButtonWidget;

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
        this.hasSettings = overlay.hasModifiableOptions();
        this.isCustom = overlay.isCustom();
        this.asTemplate = overlay.asTemplate();

    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        descriptionWidget = new TextBoxEditable(
                new WidgetInfoTextBoxEditable()
                        .pos(startX + 5,startY + 39)
                        .size(92, 54)
                        .setText(overlay.getDescription())
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTextScale(0.6f)

                        .setBackground(OptionTextures.GRAY_TEXTURE)

        );
        descriptionWidget.setReadOnly(true);

        if (!hasSettings) {
            return getWidgets();
        }
        copyButtonWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX + 112,startY + 99)
                        .size(17, 17)
                        .textures(
                                SettingsTextures.COPY_BUTTON,
                                SettingsTextures.COPY_BUTTON_HOVERED,
                                null,null,
                                SettingsTextures.COPY_BUTTON_INACTIVE
                        )
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.copy_options.tooltip"))),
                (button)->{
                    var selectedEntry = optionsListWidget.getSelectedEntry();
                    OverlayOptionGroup<?> options = optionsMap.get(selectedEntry.getId());
                    if (options == null) return;
                    if(!options.canCopy()) return;
                    owner.setCopiedOptionGroup(options);
                    widgetsChanged();
                }
        );
        pasteButtonWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX + 112, startY + 124)
                        .size(17, 17)
                        .textures(
                                SettingsTextures.PASTE_BUTTON,
                                SettingsTextures.PASTE_BUTTON_HOVERED,
                                null,null,
                                SettingsTextures.PASTE_BUTTON_INACTIVE
                        )
                        .setTooltip(
                                Tooltip.create(
                                        Component.translatable(
                                                "visor.overlay.options.overlays.paste_options.tooltip"
                                        )
                                )
                        ),
                (button)->{
                    var optionsToCopy = owner.getCopiedOptionGroup();
                    var selectedEntry = optionsListWidget.getSelectedEntry();
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

        loadFromFileButtonWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX + 112,startY + 156)
                        .size(17, 17)
                        .setTexture(SettingsTextures.BUTTON_LOAD)
                        .setTextureHovered(SettingsTextures.BUTTON_LOAD_HOVERED)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.load.tooltip"))),
                button -> {
                    overlay.reloadOptions();
                }
        );
        var rawEntries = new LinkedHashMap<String, String>();
        optionsMap = new HashMap<>();
        for (var entry : overlay.getOptions()) {
            if(!entry.isModifiable()){
                continue;
            }
            rawEntries.put(entry.getId(), entry.getDisplayName().getString());
            optionsMap.put(entry.getId(), entry);
        }
        optionsListWidget = new TexturedSelectionList(
                new WidgetInfoSelectionList()
                        .pos(startX + 5, startY + 110)
                        .size(93, 58)
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE)
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

        loadDefaultsWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX,startY + 176)
                        .size(102, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.overlays.load_defaults")),
                (it) -> {
                    loadDefaults();
                }
        );
        saveButtonWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(this.isCustom ? startX : startX + 9,startY + 195)
                        .size(83, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.overlays.save")),
                (it) -> {
                    saveChanges();
                }
        );
        if (!isCustom) {
            return getWidgets();
        }

        //CUSTOM ONLY
        removeButtonWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX + 87,startY + 195)
                        .size(15, 15)
                        .setTexture(SettingsTextures.REMOVE_BUTTON)
                        .setTextureHovered(SettingsTextures.REMOVE_BUTTON_HOVERED),
                (it) -> {
                    confirmRemove = true;
                    widgetsChanged();
                }
        );

        confirmRemoveWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX,startY + 195)
                        .size(83, 15)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(Component.translatable("visor.overlay.options.overlays.confirm_remove")),
                (it) -> {
                    confirmRemove = false;
                    owner.removeOverlay(overlay);
                }
        );
        cancelRemoveWidget = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX + 87, startY + 195)
                        .size(15, 15)
                        .setTexture(SettingsTextures.CANCEL_BUTTON)
                        .setTextureHovered(SettingsTextures.CANCEL_BUTTON_HOVERED),
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
        }else{
            list.add((T) saveButtonWidget);
        }
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //PREPARE WIDGETS
        if(hasSettings){
            var selectedEntry = optionsListWidget.getSelectedEntry();
            OverlayOptionGroup<?> options = null;
            if(selectedEntry != null){
                options = optionsMap.get(selectedEntry.getId());;
            }

            copyButtonWidget.visible = options != null;
            copyButtonWidget.active = options != null
                    && options.canCopy();

            if(owner.getCopiedOptionGroup() != null) {
                pasteButtonWidget.active = options != null
                        && options.canCopyFrom(owner.getCopiedOptionGroup());
            }
            if(!confirmRemove){
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
                    widgetInfo.getTextSizeOffset().x = -26;
                    widgetInfo.setTexture(
                            SettingsTextures.BUTTON_SAVE_WARNING
                    );
                    saveButtonWidget.setTooltip(
                            Tooltip.create(
                                    Component.translatable(
                                            "visor.overlay.options.overlays.save.tooltip_not_saved"
                                    )
                            )
                    );
                    copyButtonWidget.setTooltip(
                            Tooltip.create(
                                    Component.translatable(
                                            "visor.overlay.options.overlays.copy.tooltip_not_saved"
                                    )
                            )
                    );
                    pasteButtonWidget.setTooltip(
                            Tooltip.create(
                                    Component.translatable(
                                            "visor.overlay.options.overlays.paste.tooltip_not_saved"
                                    )
                            )
                    );
                }else{
                    widgetInfo.getTextSizeOffset().x = 0;
                    widgetInfo.getTextPosOffset().x = 0;
                    widgetInfo.setTexture(
                            OptionTextures.BLACK_TEXTURE
                    );
                    saveButtonWidget.setTooltip(null);
                    copyButtonWidget.setTooltip(null);
                    pasteButtonWidget.setTooltip(null);
                }
            }
        }

        //RENDERING
        Font font = Minecraft.getInstance().font;
        int textColor = VROverlaySettings.TEXT_COLOR.asInt();
        var icon = overlay.getIcon();
        GuiTexture labelTexture =
                this.isCustom
                        ? SettingsTextures.LABEL_CUSTOM
                        : SettingsTextures.LABEL_BUILT_IN;
        Component addonText = Component.translatable("visor.overlay.options.overlays.addon", overlay.getOwner().getAddonName());
        Component idText = Component.translatable("visor.overlay.options.overlays.id", overlay.getId());

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

        //overlay description
        descriptionWidget.setValue(overlay.getDescription().getString());

        //Overlay label
        labelTexture.blit(
                guiGraphics,
                startX + 5,
                startY + 25,
                10, 10
        );
        //Overlay template
        if (this.isCustom) {
            Component templateText = Component.translatable("visor.overlay.options.overlays.template", asTemplate.getTemplateName().getString());

            OptionTextures.GRAY_TEXTURE.blit(
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
            OptionTextures.BLACK_TEXTURE.blit(
                    guiGraphics,
                    startX,
                    startY + 99,
                    102, 111
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.overlays.options_not_found")
                            .getString(),
                    textColor,
                    startX + 15,
                    startY + 152,
                    72, 8,
                    true
            );
        } else {
            OptionTextures.BLACK_TEXTURE.blit(
                    guiGraphics,
                    startX,
                    startY + 99,
                    102, 73
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    font,
                    Component.translatable("visor.overlay.options.overlays.select_options")
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
        var entry = optionsListWidget.getSelectedEntry();
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

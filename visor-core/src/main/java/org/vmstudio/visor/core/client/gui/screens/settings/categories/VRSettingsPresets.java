package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.settings.VRSettingsPreset;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.TextBoxEditable;
import org.vmstudio.visor.api.client.gui.widgets.info.*;
import org.vmstudio.visor.api.client.gui.widgets.lists.CheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.component.ComponentIds;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.api.client.gui.settings.VRPresetSettingsType;
import org.vmstudio.visor.core.client.settings.presets.types.VRSettingsPresetCustom;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsPresets extends VROptionsSet {


    GuiTexture ENTRY_TEXTURE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(42,42,42,255)
    );
    GuiTexture BLACK_TEXTURE = TexturesHelper.getColorGuiTexture(
            AtumColor.immutable(22,22,22,255)
    );

    public static final GuiTexture BACKGROUND_BUILT_IN = new GuiTexture(
            McVersionUtils.newResourceLoc(
                    "visor:textures/gui/settings/presets_built_in_background.png"
            ),
            0, 0,
            144, 125,
            144, 125
    );
    public static final GuiTexture BACKGROUND_CUSTOM = new GuiTexture(
            McVersionUtils.newResourceLoc(
                    "visor:textures/gui/settings/presets_custom_background.png"
            ),
            0, 0,
            144, 125,
            144, 125
    );
    public static final GuiTexture BACKGROUND_CREATE = new GuiTexture(
            McVersionUtils.newResourceLoc(
                    "visor:textures/gui/settings/presets_create_background.png"
            ),
            0, 0,
            144, 125,
            144, 125
    );


    private static final ResourceLocation RESOURCE = McVersionUtils.newResourceLoc(
            "visor:textures/gui/settings/general.png"
    );
    private static final int RESOURCE_WIDTH = 274;
    private static final int RESOURCE_HEIGHT = 260;

    public static final GuiTexture PRESETS_FOLDER = new GuiTexture(
            RESOURCE,
            209, 108,
            53, 25,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );

    public static final GuiTexture CHECKBOX = new GuiTexture(
            RESOURCE,
            216, 135,
            6, 6,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture CHECKBOX_HOVERED = new GuiTexture(
            RESOURCE,
            230, 135,
            6, 6,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture CHECKBOX_SELECTED = new GuiTexture(
            RESOURCE,
            209, 135,
            6, 6,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture CHECKBOX_HOVERED_SELECTED = new GuiTexture(
            RESOURCE,
            223, 135,
            6, 6,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );
    public static final GuiTexture CANCEL_REMOVAL = new GuiTexture(
            RESOURCE,
            209, 144,
            51, 22,
            RESOURCE_WIDTH, RESOURCE_HEIGHT
    );


    private DynamicWidgetSet submenuWidgetSet;

    public VRSettingsPresets(@NotNull VRSettingsScreen screen,
                             @Nullable VROptionsSet previousOptions,
                             @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
        submenuWidgetSet = new BuiltInWidgetSet(onWidgetsChanged);

    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        return null;
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return null;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        submenuWidgetSet.initWidgets();
        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        return new ArrayList<>(submenuWidgetSet.getWidgets());
    }

    @Override
    public void onTick() {
        submenuWidgetSet.onTick();
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
        submenuWidgetSet.onPreRender(guiGraphics,mouseX, mouseY, partialTicks);
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics,
                             int mouseX, int mouseY,
                             float partialTicks) {
        submenuWidgetSet.onPostRender(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void previousOptions() {
        if(submenuWidgetSet instanceof CreateWidgetSet){
            submenuWidgetSet = new CustomWidgetSet(onWidgetsChanged);
            reinit();
            return;
        }
        super.previousOptions();
    }

    @Override
    public boolean canOpenPreviousPage() {
        if(submenuWidgetSet instanceof CreateWidgetSet){
            return true;
        }
        return super.canOpenPreviousPage();
    }

    @Override
    protected boolean canLoadDefaults() {
        return false;
    }


    private class BuiltInWidgetSet extends DynamicWidgetSet{

        private TexturedSelectionList listWidget;
        private TextBoxEditable descriptionTextBox;

        private ButtonImaged builtInButton;
        private ButtonImaged customButton;
        private ButtonImaged applyButton;


        private VRSettingsPreset selectedPreset;

        public BuiltInWidgetSet(@NotNull Runnable onWidgetsChanged) {
            super(onWidgetsChanged);
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> initWidgets() {
            var scaleHelper = getScreen().getScaleHelper();

            Map<String, String> rawEntries = new LinkedHashMap<>();
            Map<String, VRSettingsPreset> entries = new HashMap<>();

            for(var entry : ClientContext.settingsManager
                    .getPresetsRegistry().getAllComponents()){
                if(!entry.isBuiltIn()) continue;
                String name = entry.getName().getString();
                rawEntries.put(entry.getId(), name);
                entries.put(entry.getId(), entry);
            }

            listWidget = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(scaleHelper.scaledX(59), scaleHelper.scaledY(44))
                            .size(scaleHelper.scaledSize(72), scaleHelper.scaledSize(88))
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(ENTRY_TEXTURE)
                                            .highlight(
                                                    OptionTextures.HOVERED_HIGHLIGHT,
                                                    OptionTextures.SELECTED_HIGHLIGHT
                                            )
                            )
                            .setTooltip(
                                    (it)->{
                                        var entry = entries.get(it);
                                        String id = Component.translatable("visor.options.presets.id").getString()
                                                +": "+entry.getId();
                                        String addon = Component.translatable("visor.options.presets.addon").getString()
                                                +": "+entry.getOwner().getAddonName().getString();
                                        return Component.literal(id+"\n"+addon);
                                    }
                            )
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    rawEntries,
                    it -> {
                        if(it == null){
                            return;
                        }
                        selectedPreset = entries.get(it.getId());
                        descriptionTextBox.setValue(selectedPreset.getDescription().getString());
                    }
            );

            builtInButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(58),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        //Do nothing
                    }
            );

            customButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(131),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        switchToCustom();
                    }
            );
            applyButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(94),scaleHelper.scaledY(136))
                            .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                            .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                            .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                            .setTextScale(VRClientSettings.getSettingsTextScale())
                            .setText(Component.translatable(
                                    "visor.options.presets.apply_preset"
                            ))
                            .setTooltip(Tooltip.create(Component.translatable(
                                    "visor.options.presets.apply_preset.tooltip"
                            ))),
                    (it)->{
                        applyPreset();
                    }
            );


            descriptionTextBox = new TextBoxEditable(
                    new WidgetInfoTextBoxEditable()
                            .pos(scaleHelper.scaledX(137),scaleHelper.scaledY(58))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(72))
                            .setText(Component.literal("????"))
                            .setTextScale(0.8f)
            );
            descriptionTextBox.setReadOnly(true);

            return List.of();
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> getWidgets() {
            List<T> list = new ArrayList<>();
            list.add((T) listWidget);
            list.add((T) descriptionTextBox);
            list.add((T) builtInButton);
            list.add((T) customButton);
            list.add((T) applyButton);
            return list;
        }

        @Override
        public void onTick() {

        }

        @Override
        public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();
            BACKGROUND_BUILT_IN.blit(
                    guiGraphics,
                    scaleHelper.scaledX(56),
                    scaleHelper.scaledY(26),
                    scaleHelper.scaledSize(BACKGROUND_BUILT_IN.getWidth()),
                    scaleHelper.scaledSize(BACKGROUND_BUILT_IN.getHeight())
            );
            applyButton.active = selectedPreset != null;
        }

        @Override
        public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();

            //built in
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.built_in").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(69), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );
            //custom
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.custom").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(142), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );


            String name = selectedPreset == null
                    ? "????"
                    :  selectedPreset.getName().getString();
            //name
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    name,
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(137), scaleHelper.scaledY(48),
                    scaleHelper.scaledSize(58), scaleHelper.scaledSize(7),
                    true
            );
        }

        private void switchToCustom(){
            submenuWidgetSet = new CustomWidgetSet(onWidgetsChanged);
            reinit();
        }
        private void applyPreset(){
            if(selectedPreset != null) {
                selectedPreset.apply();
            }
        }


    }

    //Don't want to add "save" for saving of settings for already created preset,
    //cause it will give a room for confusion when there
    // is a description not aligning with the result
    private class CustomWidgetSet extends DynamicWidgetSet{
        private TexturedSelectionList listWidget;
        private TexturedSelectionList settingTypesList;
        private TextBoxEditable descriptionTextBox;

        private ButtonImaged builtInButton;
        private ButtonImaged customButton;
        private ButtonImaged createButton;
        private ButtonImaged presetsFolderButton;
        private ButtonImaged applyButton;

        private ButtonImaged removeButton;
        private ButtonImaged removeConfirmButton;
        private ButtonImaged removeCancelButton;


        private VRSettingsPreset selectedPreset;

        public CustomWidgetSet(@NotNull Runnable onWidgetsChanged) {
            super(onWidgetsChanged);
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> initWidgets() {
            var scaleHelper = getScreen().getScaleHelper();

            Map<String, String> rawEntries = new LinkedHashMap<>();
            Map<String, VRSettingsPreset> entries = new HashMap<>();

            for(var entry : ClientContext.settingsManager
                    .getPresetsRegistry().getAllComponents()){
                if(!entry.isCustom()) continue;
                String name = entry.getName().getString();
                rawEntries.put(entry.getId(), name);
                entries.put(entry.getId(), entry);
            }

            listWidget = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(scaleHelper.scaledX(59), scaleHelper.scaledY(44))
                            .size(scaleHelper.scaledSize(72), scaleHelper.scaledSize(88))
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(ENTRY_TEXTURE)
                                            .highlight(
                                                    OptionTextures.HOVERED_HIGHLIGHT,
                                                    OptionTextures.SELECTED_HIGHLIGHT
                                            )
                            )
                            .setTooltip(
                                    (it)->{
                                        var entry = entries.get(it);
                                        String id = Component.translatable("visor.options.presets.id").getString()
                                                +": "+entry.getId();
                                        String version = "Visor: "+((VRSettingsPresetCustom)entry).getOriginVisorVersion();
                                        return Component.literal(id+"\n"+version);
                                    }
                            )
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    rawEntries,
                    it -> {
                        if(it == null){
                            return;
                        }
                        selectedPreset = entries.get(it.getId());
                        presetSelected();

                    }
            );

            settingTypesList = new TexturedSelectionList(
                    new WidgetInfoSelectionList()
                            .pos(scaleHelper.scaledX(137), scaleHelper.scaledY(87))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(36))
                            .setEntryButton(
                                    new WidgetInfoButtonImaged()
                                            .setTexture(ENTRY_TEXTURE)
                            )
                            .setSupportDeselection(true)
                            .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                    new HashMap<>(),
                    it -> {
                        if(it == null){
                            return;
                        }
                        settingTypesList.setSelectedEntry((TexturedSelectionList.TexturedEntry) null);
                    }
            );

            builtInButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(58),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        switchToBuiltIn();
                    }
            );

            customButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(131),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        //Do nothing
                    }
            );

            createButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(65),scaleHelper.scaledY(136))
                            .size(scaleHelper.scaledSize(21), scaleHelper.scaledSize(10))
                            .setTexture(VRSettingsScreen.ADD_BUTTON)
                            .setTooltip(Tooltip.create(Component.translatable(
                                    "visor.options.presets.create_preset.tooltip"
                            )))
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                    (it)->{
                        openCreationMenu();
                    }
            );
            presetsFolderButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(166),scaleHelper.scaledY(136))
                            .size(scaleHelper.scaledSize(21), scaleHelper.scaledSize(10))
                            .setTexture(PRESETS_FOLDER)
                            .setTooltip(Tooltip.create(Component.translatable(
                                    "visor.options.presets.presets_folder.tooltip"
                            )))
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT),
                    (it)->{
                        openPresetsFolder();
                    }
            );

            applyButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(94),scaleHelper.scaledY(136))
                            .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                            .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                            .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                            .setTextScale(VRClientSettings.getSettingsTextScale())
                            .setText(Component.translatable(
                                    "visor.options.presets.apply_preset"
                            ))
                            .setTooltip(Tooltip.create(Component.translatable(
                                    "visor.options.presets.apply_preset.tooltip"
                            ))),
                    (it)->{
                        applyPreset();
                    }
            );

            removeButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(137),scaleHelper.scaledY(125))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(6))
                            .setTexture(BLACK_TEXTURE)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                            .setDynamicTextScale(true)
                            .setText(Component.translatable(
                                    "visor.options.presets.remove"
                            )),
                    (it)->{
                        removePressed();
                    }
            );

            removeConfirmButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(137),scaleHelper.scaledY(125))
                            .size(scaleHelper.scaledSize(39), scaleHelper.scaledSize(6))
                            .setTexture(BLACK_TEXTURE)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                            .setDynamicTextScale(true)
                            .setText(Component.translatable(
                                    "visor.options.presets.confirm_remove"
                            )),
                    (it)->{
                        removeConfirmed();
                    }
            );
            removeCancelButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(181),scaleHelper.scaledY(125))
                            .size(scaleHelper.scaledSize(14), scaleHelper.scaledSize(6))
                            .setTexture(CANCEL_REMOVAL)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                    ,
                    (it)->{
                        removeCancelled();
                    }
            );

            descriptionTextBox = new TextBoxEditable(
                    new WidgetInfoTextBoxEditable()
                            .pos(scaleHelper.scaledX(137),scaleHelper.scaledY(56))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(23))
                            .setText(Component.literal("????"))
                            .setTextScale(0.8f)

            );
            descriptionTextBox.setReadOnly(true);

            removeButton.visible = false;
            removeConfirmButton.visible = false;
            removeCancelButton.visible = false;

            return List.of();
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> getWidgets() {
            List<T> list = new ArrayList<>();
            list.add((T) listWidget);
            list.add((T) settingTypesList);
            list.add((T) descriptionTextBox);
            list.add((T) builtInButton);
            list.add((T) customButton);
            list.add((T) createButton);
            list.add((T) presetsFolderButton);
            list.add((T) applyButton);
            list.add((T) removeButton);
            list.add((T) removeConfirmButton);
            list.add((T) removeCancelButton);
            return list;
        }

        @Override
        public void onTick() {

        }

        @Override
        public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();
            BACKGROUND_CUSTOM.blit(
                    guiGraphics,
                    scaleHelper.scaledX(56),
                    scaleHelper.scaledY(26),
                    scaleHelper.scaledSize(BACKGROUND_CUSTOM.getWidth()),
                    scaleHelper.scaledSize(BACKGROUND_CUSTOM.getHeight())
            );
            applyButton.active = selectedPreset != null;

        }

        @Override
        public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();

            //built in
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.built_in").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(69), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );
            //custom
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.custom").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(142), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );


            String name = selectedPreset == null
                    ? "????"
                    :  selectedPreset.getName().getString();
            //name
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    name,
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(137), scaleHelper.scaledY(47),
                    scaleHelper.scaledSize(58), scaleHelper.scaledSize(7),
                    true
            );

            //settings saved
            if(selectedPreset != null) {
                GuiHelper.renderScalableText(
                        guiGraphics,
                        MC.font,
                        Component.translatable("visor.options.presets.saved_settings").getString(),
                        AtumColor.WHITE.asInt(),
                        scaleHelper.scaledX(143), scaleHelper.scaledY(81),
                        scaleHelper.scaledSize(46), scaleHelper.scaledSize(5),
                        true
                );
            }
        }

        private void presetSelected(){
            descriptionTextBox.setValue(selectedPreset.getDescription().getString());

            Map<String, String> rawEntries = new LinkedHashMap<>();
            for(var type : ((VRSettingsPresetCustom)selectedPreset).getSettingTypes()){
                rawEntries.put(type.getKey(), type.getName().getString());
            }
            settingTypesList.resetEntries(rawEntries);
            removeButton.visible = true;
            removeConfirmButton.visible = false;
        }

        private void switchToBuiltIn(){
            submenuWidgetSet = new BuiltInWidgetSet(onWidgetsChanged);
            reinit();
        }
        private void openCreationMenu(){
            submenuWidgetSet = new CreateWidgetSet(onWidgetsChanged);
            reinit();
        }
        private void openPresetsFolder(){
            var dir = ClientContext.settingsManager.getPresetsCatalog()
                    .getDirectory().toUri();

            Util.getPlatform().openUri(
                    dir
            );
        }
        private void applyPreset(){
            if(selectedPreset != null) {
                selectedPreset.apply();
            }
        }
        private void removePressed(){
            removeButton.visible = false;
            removeConfirmButton.visible = true;
            removeCancelButton.visible = true;
        }
        private void removeConfirmed(){
            ClientContext.settingsManager.getPresetsRegistry().deleteCustomPreset(
                    selectedPreset.getId()
            );
            reinit();
        }
        private void removeCancelled(){
            removeButton.visible = true;
            removeConfirmButton.visible = false;
            removeCancelButton.visible = false;
        }
    }

    private class CreateWidgetSet extends DynamicWidgetSet{
        private CheckboxList settingsTypesList;

        private ButtonImaged builtInButton;
        private ButtonImaged customButton;
        private ButtonImaged confirmButton;

        private EditBoxImaged idEditBix;
        private EditBoxImaged nameEditBix;
        private TextBoxEditable descriptionTextBox;

        private List<VRPresetSettingsType> selectedSettingsTypes;

        public CreateWidgetSet(@NotNull Runnable onWidgetsChanged) {
            super(onWidgetsChanged);
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> initWidgets() {
            String prevId   = idEditBix == null ? "" : idEditBix.getValue();
            String prevName = nameEditBix == null ? "" : nameEditBix.getValue();
            String prevDesc = descriptionTextBox == null ? "" : descriptionTextBox.getValue();
            List<VRPresetSettingsType> prevTypes =
                    selectedSettingsTypes == null ? new ArrayList<>() : selectedSettingsTypes;

            selectedSettingsTypes = new ArrayList<>(prevTypes);
            var selectedKeys = prevTypes.stream().map(VRPresetSettingsType::getKey).toList();

            var scaleHelper = getScreen().getScaleHelper();

            var rawEntries = new LinkedHashMap<String, String>();

            for(var type : VRPresetSettingsType.values()){
                rawEntries.put(type.getKey(), type.getName().getString());
            }

            settingsTypesList = new CheckboxList(
                    new WidgetInfoCheckboxList()
                            .pos(scaleHelper.scaledX(134),scaleHelper.scaledY(53))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(79))
                            .setTextColor(AtumColor.WHITE)
                            .setCheckboxLeftSided(false)
                            .textures(
                                    ENTRY_TEXTURE,
                                    CHECKBOX,
                                    CHECKBOX_HOVERED,
                                    CHECKBOX_SELECTED,
                                    CHECKBOX_HOVERED_SELECTED
                            ),
                    rawEntries,
                    selectedKeys,
                    (it)->{
                        var type = VRPresetSettingsType.fromId(it.getId());
                        if(it.isSelected() && !selectedSettingsTypes.contains(type)){
                            selectedSettingsTypes.add(
                                    type
                            );
                        }else if(!it.isSelected()){
                            selectedSettingsTypes.remove(type);
                        }
                    }

            );

            builtInButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(58),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        switchToBuiltIn();
                    }
            );

            customButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(131),scaleHelper.scaledY(27))
                            .size(scaleHelper.scaledSize(67), scaleHelper.scaledSize(11)),
                    (it)->{
                        //Do nothing
                    }
            );

            confirmButton = new ButtonImaged(
                    new WidgetInfoButtonImaged()
                            .pos(scaleHelper.scaledX(94),scaleHelper.scaledY(136))
                            .size(scaleHelper.scaledSize(64), scaleHelper.scaledSize(10))
                            .setTexture(OptionTextures.LIGHT_GRAY_TEXTURE_2)
                            .setTextureInactive(OptionTextures.GRAY_TEXTURE)
                            .setHighlightEnabled(true)
                            .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                            .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                            .setTextScale(VRClientSettings.getSettingsTextScale())
                            .setText(Component.translatable(
                                    "visor.options.presets.confirm_creation"
                                    )
                            ),
                    (it)->{
                        confirmPressed();
                    }
            );

            idEditBix = new EditBoxImaged(
                    new WidgetInfoEditBox()
                            .pos(scaleHelper.scaledX(63), scaleHelper.scaledY(53))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(7))
                            .setTexture(null)
            );

            nameEditBix = new EditBoxImaged(
                    new WidgetInfoEditBox()
                            .pos(scaleHelper.scaledX(63), scaleHelper.scaledY(69))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(7))
                            .setTexture(null)
            );

            descriptionTextBox = new TextBoxEditable(
                    new WidgetInfoTextBoxEditable()
                            .pos(scaleHelper.scaledX(63),scaleHelper.scaledY(91))
                            .size(scaleHelper.scaledSize(58), scaleHelper.scaledSize(23))
                            .setText(Component.literal(""))
                            .setTextScale(0.8f)

            );

            confirmButton.active = false;

            idEditBix.setValue(prevId);
            nameEditBix.setValue(prevName);
            descriptionTextBox.setValue(prevDesc);

            return List.of();
        }

        @Override
        public <T extends GuiEventListener
                & Renderable
                & NarratableEntry> List<T> getWidgets() {
            List<T> list = new ArrayList<>();
            list.add((T) settingsTypesList);
            list.add((T) builtInButton);
            list.add((T) customButton);
            list.add((T) confirmButton);
            list.add((T) idEditBix);
            list.add((T) nameEditBix);
            list.add((T) descriptionTextBox);
            return list;
        }

        @Override
        public void onTick() {
            updateConfirmState();
            descriptionTextBox.tick();
        }

        @Override
        public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();
            BACKGROUND_CREATE.blit(
                    guiGraphics,
                    scaleHelper.scaledX(56),
                    scaleHelper.scaledY(26),
                    scaleHelper.scaledSize(BACKGROUND_CREATE.getWidth()),
                    scaleHelper.scaledSize(BACKGROUND_CREATE.getHeight())
            );
        }

        @Override
        public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            var scaleHelper = getScreen().getScaleHelper();

            //built in
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.built_in").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(69), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );
            //custom
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.custom").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(142), scaleHelper.scaledY(29),
                    scaleHelper.scaledSize(45), scaleHelper.scaledSize(7),
                    true
            );
            //id
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.id").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(76), scaleHelper.scaledY(47),
                    scaleHelper.scaledSize(32), scaleHelper.scaledSize(5),
                    true
            );
            //name
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.name").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(76), scaleHelper.scaledY(63),
                    scaleHelper.scaledSize(32), scaleHelper.scaledSize(5),
                    true
            );
            //description
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.description").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(76), scaleHelper.scaledY(85),
                    scaleHelper.scaledSize(32), scaleHelper.scaledSize(5),
                    true
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    MC.font,
                    Component.translatable("visor.options.presets.specify_settings").getString(),
                    AtumColor.WHITE.asInt(),
                    scaleHelper.scaledX(143), scaleHelper.scaledY(47),
                    scaleHelper.scaledSize(40), scaleHelper.scaledSize(5),
                    true
            );
        }
        private void switchToBuiltIn(){
            submenuWidgetSet = new BuiltInWidgetSet(onWidgetsChanged);
            reinit();
        }

        private void confirmPressed(){
            updateConfirmState();
            if(!confirmButton.active){
                return;
            }

            String id = idEditBix.getValue();
            String name = nameEditBix.getValue();
            String description = descriptionTextBox.getValue();

            try {
                VRSettingsPresetCustom.createNew(
                        id,
                        name, description,
                        selectedSettingsTypes
                );
                submenuWidgetSet = new CustomWidgetSet(onWidgetsChanged);
                reinit();
            } catch (Exception e) {
                VisorClientImpl.LOGGER.error("Failed to create preset '{}'", id, e);
                idEditBix.setTextColor(AtumColor.RED.asInt());
                confirmButton.active = false;
            }
        }

        private void updateConfirmState(){
            var registry = ClientContext.settingsManager.getPresetsRegistry();
            String id = idEditBix.getValue();
            String name = nameEditBix.getValue();

            confirmButton.active = true;

            if(selectedSettingsTypes.isEmpty()){
                confirmButton.active = false;
            }

            if(id.length() < 3){
                confirmButton.active = false;
                if(!id.isEmpty()) {
                    idEditBix.setTextColor(AtumColor.RED.asInt());
                }
            }
            else if(registry.getComponent(id) != null){
                confirmButton.active = false;
                idEditBix.setTextColor(AtumColor.RED.asInt());
            }else if(!ComponentIds.isValid(id)){
                confirmButton.active = false;
                idEditBix.setTextColor(AtumColor.RED.asInt());
            }else{
                idEditBix.setTextColor(AtumColor.WHITE.asInt());
            }
            if(name.length() < 3){
                confirmButton.active = false;
                if(!name.isEmpty()) {
                    nameEditBix.setTextColor(AtumColor.RED.asInt());
                }
            }else{
                nameEditBix.setTextColor(AtumColor.WHITE.asInt());
            }

        }
    }
}

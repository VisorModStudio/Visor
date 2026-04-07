package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoCheckboxList;
import org.vmstudio.visor.api.client.gui.widgets.lists.CheckboxList;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.SettingsTextures;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayoutId;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayouts;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsKeyboardLayouts extends VROptionsSet {

    private CheckboxList listWidget;

    public VRSettingsKeyboardLayouts(@NotNull VRSettingsScreen screen,
                                     @Nullable VROptionsSet previousOptions,
                                     @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
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
        var scaleHelper = getScreen().getScaleHelper();

        Map<String, String> rawEntries = new LinkedHashMap<>();
        for (KeyboardLayoutId layoutId : KeyboardLayouts.getSelectableLayouts()) {
            rawEntries.put(layoutId.name(), layoutId.getDisplayName());
        }

        List<String> selectedIds = VRClientSettings.getEnabledKeyboardLayouts()
                .stream()
                .map(Enum::name)
                .toList();

        listWidget = new CheckboxList(
                new WidgetInfoCheckboxList()
                        .pos(scaleHelper.scaledX(57), scaleHelper.scaledY(43))
                        .size(scaleHelper.scaledSize(142), scaleHelper.scaledSize(90))
                        .textures(
                                OptionTextures.GRAY_TEXTURE,
                                SettingsTextures.CHECKBOX_BUTTON,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED,
                                SettingsTextures.CHECKBOX_BUTTON_SELECTED,
                                SettingsTextures.CHECKBOX_BUTTON_HOVERED_SELECTED
                        ),
                rawEntries,
                selectedIds,
                (entry) -> saveSelectedLayouts()
        );

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) listWidget);
        return list;
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics,
                             int mouseX, int mouseY,
                             float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.options.controls.keyboard_layouts.title").getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(84), scaleHelper.scaledY(30),
                scaleHelper.scaledSize(86), scaleHelper.scaledSize(10),
                true
        );
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.options.controls.keyboard_layouts.always_enabled").getString(),
                VRSettingsScreen.INACTIVE_COLOR.asInt(),
                scaleHelper.scaledX(60), scaleHelper.scaledY(136),
                scaleHelper.scaledSize(126), scaleHelper.scaledSize(8),
                true
        );
    }

    @Override
    public void loadDefaults() {
        VRClientSettings.setEnabledKeyboardLayouts(List.of(KeyboardLayoutId.EN_US));
        ClientContext.settingsManager.saveOptions();
        reinit();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        listWidget.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void saveSelectedLayouts() {
        List<KeyboardLayoutId> enabledLayouts = new ArrayList<>();
        for (String selectedId : listWidget.getSelectedEntriesId()) {
            KeyboardLayoutId layoutId = KeyboardLayoutId.byName(selectedId);
            if (layoutId != null) {
                enabledLayouts.add(layoutId);
            }
        }
        VRClientSettings.setEnabledKeyboardLayouts(enabledLayouts);
        ClientContext.settingsManager.saveOptions();
    }
}
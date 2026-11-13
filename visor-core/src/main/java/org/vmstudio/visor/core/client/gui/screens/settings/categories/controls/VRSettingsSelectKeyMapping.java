package org.vmstudio.visor.core.client.gui.screens.settings.categories.controls;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.sets.SearchableListWidgetSet;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsSelectKeyMapping extends VROptionsSet {

    private final VRSettingsCreateCustomAction parentScreen;

    private SearchableListWidgetSet keyMappingsList;

    public VRSettingsSelectKeyMapping(@NotNull VRSettingsCreateCustomAction parentScreen,
                                      @NotNull Runnable onWidgetsChanged) {
        super(parentScreen.getScreen(), parentScreen, onWidgetsChanged);
        this.parentScreen = parentScreen;
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
    protected boolean canLoadDefaults() {
        return false;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        var scaleHelper = getScreen().getScaleHelper();

        KeyMapping[] keyMappings = MC.options.keyMappings.clone();
        Arrays.sort(keyMappings);
        Map<String, String> entries = new LinkedHashMap<>();
        for(var keyMapping : keyMappings){
            if(!InputHelper.canChangeStateForKey(keyMapping)) continue;
            entries.put(
                    keyMapping.getName(),
                    Component.translatable(keyMapping.getName()).getString()
            );
        }

        keyMappingsList = new SearchableListWidgetSet.Builder(
                new WidgetInfoSelectionList()
                        .pos(scaleHelper.scaledX(60), scaleHelper.scaledY(57))
                        .size(scaleHelper.scaledSize(136), scaleHelper.scaledSize(74))
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTooltip(this::entryTooltip)
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                entries,
                (it)->{
                    if(it == null) return;
                    parentScreen.selectKeyMapping(it.getId());
                    switchOptions(parentScreen);
                },
                onWidgetsChanged
        ).searchBox(
                new WidgetInfoEditBox()
                        .pos(scaleHelper.scaledX(60), scaleHelper.scaledY(44))
                        .size(scaleHelper.scaledSize(136), scaleHelper.scaledSize(10))
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(VROverlaySettings.TEXT_FIND)
        ).build();
        keyMappingsList.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        return keyMappingsList.getWidgets();
    }

    @Override
    public void onTick() {
        keyMappingsList.onTick();
    }

    @Override
    public void onPreRender(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        keyMappingsList.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onPostRender(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();

        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.action.options.select_keybind").getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(60), scaleHelper.scaledY(31),
                scaleHelper.scaledSize(136), scaleHelper.scaledSize(7),
                true
        );
    }

    private Component entryTooltip(@NotNull String name){
        KeyMapping keyMapping = InputHelper.getKeyMapping(name);
        if(keyMapping == null){
            return Component.empty();
        }
        return Component.translatable(
                "visor.action.options.select_keybind.entry.tooltip",
                Component.translatable(name),
                McVersionClientUtils.keyMappingCategory(keyMapping),
                keyMapping.getTranslatedKeyMessage()
        );
    }
}

package org.vmstudio.visor.core.client.gui.screens.settings.categories;

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
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSelectionList;
import org.vmstudio.visor.api.client.gui.widgets.lists.TexturedSelectionList;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.controls.VRSettingsActions;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsBody extends VROptionsSet {

    private TexturedSelectionList listWidget;

    public VRSettingsBody(@NotNull VRSettingsScreen screen,
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
    protected boolean canLoadDefaults() {
        return false;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        var scaleHelper = getScreen().getScaleHelper();
        Collection<VRBody> sets = ClientContext.decorationRenderer
                .getVrBodyRegistry()
                .getAllComponents();
        Map<String, String> rawEntries = new LinkedHashMap<>();
        Map<String, VRBody> entriesLink = new LinkedHashMap<>();
        for(var body : sets){
            if(!body.isSelectable()) continue;

            rawEntries.put(body.getId(), body.getName().getString());
            entriesLink.put(body.getId(), body);
        }
        listWidget = new TexturedSelectionList(
                new WidgetInfoSelectionList()
                        .pos(scaleHelper.scaledX(57), scaleHelper.scaledY(43))
                        .size(scaleHelper.scaledSize(142), scaleHelper.scaledSize(90))
                        .setColumns(2)
                        .setEntryButton(
                                new WidgetInfoButtonImaged()
                                        .setTexture(OptionTextures.GRAY_TEXTURE)
                                        .highlight(
                                                OptionTextures.HOVERED_HIGHLIGHT,
                                                OptionTextures.SELECTED_HIGHLIGHT
                                        )
                        )
                        .setTextureScrollBarActive(OptionTextures.SCROLL_BAR_ACTIVE),
                rawEntries,
                it -> {
                    if(it == null) return;
                    if(!ClientContext.localPlayer.isBodyChangeable()){
                        return;
                    }

                    VRBody body = entriesLink.get(it.getId());

                    if (body == null) return;
                    ClientContext.localPlayer.setBody(body);
                    VRClientSettings.setDefaultVrBody(body.getId());
                    ClientContext.settingsManager.saveOptions();
                }
        );
        listWidget.setSelectedEntry(ClientContext.localPlayer.getBody().getId());

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T)listWidget);
        return list;
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable("visor.options.vr_body.select").getString(),
                AtumColor.WHITE.asInt(),
                scaleHelper.scaledX(90), scaleHelper.scaledY(30),
                scaleHelper.scaledSize(74), scaleHelper.scaledSize(10),
                true
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        listWidget.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}

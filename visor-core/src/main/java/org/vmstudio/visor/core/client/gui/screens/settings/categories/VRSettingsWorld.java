package org.vmstudio.visor.core.client.gui.screens.settings.categories;


import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.settings.VROptionCategory;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.ServerSettingsFileStore;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsWorld extends VROptionsSet {

    public VRSettingsWorld(@NotNull VRSettingsScreen screen,
                           @Nullable VROptionsSet previousOptions,
                           @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
        if(isEditable()){
            ServerSettingsFileStore.reloadIntoStatics();
        }
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {
        //two EMPTY slots reserve row 0 for the info line
        List<VROptionWidgetType> types = new ArrayList<>();
        types.add(VROptionWidgetType.EMPTY);
        types.add(VROptionWidgetType.EMPTY);
        types.addAll(VROptionWidgetType.byCategory(VROptionCategory.WORLD));
        return types.toArray(new VROptionWidgetType[0]);
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        return new OptionWidgetEntry[0];
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        super.initWidgets();
        if(!isEditable()){
            for(var child : getWidgets()){
                if(child instanceof AbstractWidget widget){
                    widget.active = false;
                }
                if(child instanceof ButtonImaged button){
                    button.getWidgetInfo().setTextColor(VRSettingsScreen.INACTIVE_COLOR);
                }
            }
        }
        return getWidgets();
    }

    @Override
    protected boolean canLoadDefaults() {
        return isEditable();
    }

    @Override
    public void loadDefaults() {
        if(!isEditable()) return;
        for (var child : getWidgets()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            var optionType = getTypeFromWidget(widget);
            if(optionType == null) continue;
            ClientContext.settingsManager
                    .loadDefaultOptionValue(
                            optionType.getKey()
                    );
        }
        ServerSettingsFileStore.save();
        reinit();
    }

    @Override
    public void onPostRender(@NotNull GuiGraphics guiGraphics,
                             int mouseX, int mouseY,
                             float partialTicks) {
        var scaleHelper = getScreen().getScaleHelper();
        boolean editable = isEditable();
        GuiHelper.renderScalableText(
                guiGraphics,
                MC.font,
                Component.translatable(editable
                        ? "visor.options.world.note.editable"
                        : "visor.options.world.note.readonly"
                ).getString(),
                (editable ? AtumColor.WHITE : VRSettingsScreen.INACTIVE_COLOR).asInt(),
                scaleHelper.scaledX(56), scaleHelper.scaledY(32),
                scaleHelper.scaledSize(144), scaleHelper.scaledSize(7),
                true
        );
    }

    private static boolean isEditable(){
        return MC.level == null;
    }
}

package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets.identity;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.TextBoxEditable;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;

import org.vmstudio.visor.api.client.gui.widgets.sets.WidgetSet;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SetupIdentityWidgetSet implements WidgetSet {


    @Getter
    private EditBoxImaged idWidget;
    @Getter
    private EditBoxImaged nameWidget;
    @Getter
    private TextBoxEditable descriptionWidget;

    @Getter
    private SetupIconWidgetSet setupIconWidget;


    private final int startX;
    private final int startY;

    private final boolean withId;

    public SetupIdentityWidgetSet(int startX, int startY, boolean withId){
        this.startX = startX;
        this.startY = startY;
        this.withId = withId;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {

        int extraY = 0;
        if(withId) {
            idWidget = new EditBoxImaged(
                    new WidgetInfoEditBox()
                            .pos(startX + 14, startY + 43)
                            .size(92, 13)
                            .setTexture(OptionTextures.GRAY_TEXTURE)
                            .setTextColor(VROverlaySettings.TEXT_COLOR)
                            .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_id"))
                            .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.create_overlay.type_id.tooltip")))
            );
            extraY = 23;
        }
        nameWidget = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX + 14,startY + 66 - 23 + extraY)
                        .size(92, 13)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_name"))
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.create_overlay.type_name.tooltip")))
        );
        descriptionWidget = new TextBoxEditable(
                new WidgetInfoTextBoxEditable()
                        .pos(startX + 14,startY + 89 - 23 + extraY)
                        .size(92, 54).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTextHintColor(VROverlaySettings.TEXT_COLOR)
                        .setTextScale(0.6f)
                        .setBackground(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_description"))
                        .setTooltip(()->Component.translatable("visor.overlay.options.overlays.create_overlay.type_description.tooltip"))
        );

        setupIconWidget = new SetupIconWidgetSet(
                startX + 6,
                startY + 151 - 23 + extraY
        );

        setupIconWidget.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        if(withId) {
            list.add((T) idWidget);
        }
        list.add((T) nameWidget);
        list.add((T) descriptionWidget);
        list.addAll(setupIconWidget.getWidgets());
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        setupIconWidget.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);

    }

    @Override
    public void onTick() {
        descriptionWidget.tick();
        setupIconWidget.onTick();
    }




}

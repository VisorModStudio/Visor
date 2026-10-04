package org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets.identity;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.TextBoxEditable;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoTextBoxEditable;

import org.vmstudio.visor.api.client.gui.widgets.sets.WidgetSet;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SetupIdentityWidgetSet implements WidgetSet {

    private static final AtumColor ID_COLOR = VROverlaySettings.TEXT_COLOR.blend(AtumColor.BLACK, 0.3f);

    @Getter
    private EditBoxImaged nameWidget;
    @Getter
    private TextBoxEditable descriptionWidget;

    @Getter
    private SetupIconWidgetSet setupIconWidget;


    private final int startX;
    private final int startY;

    private final Supplier<String> idPreview;

    public SetupIdentityWidgetSet(int startX, int startY, @Nullable Supplier<String> idPreview){
        this.startX = startX;
        this.startY = startY;
        this.idPreview = idPreview;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {

        boolean withId = idPreview != null;
        nameWidget = new EditBoxImaged(
                new WidgetInfoEditBox()
                        .pos(startX + 14,startY + 43)
                        .size(92, 13)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_name"))
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.overlays.create_overlay.type_name.tooltip")))
        );
        descriptionWidget = new TextBoxEditable(
                new WidgetInfoTextBoxEditable()
                        .pos(startX + 14,startY + (withId ? 68 : 66))
                        .size(92, withId ? 75 : 54).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setTextHintColor(VROverlaySettings.TEXT_COLOR)
                        .setTextScale(0.6f)
                        .setBackground(OptionTextures.GRAY_TEXTURE)
                        .setHint(Component.translatable("visor.overlay.options.overlays.create_overlay.type_description"))
                        .setTooltip(()->Component.translatable("visor.overlay.options.overlays.create_overlay.type_description.tooltip"))
        );

        setupIconWidget = new SetupIconWidgetSet(
                startX + 6,
                startY + (withId ? 151 : 128)
        );

        setupIconWidget.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> list = new ArrayList<>();
        list.add((T) nameWidget);
        list.add((T) descriptionWidget);
        list.addAll(setupIconWidget.getWidgets());
        return list;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphicsExtractor guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        setupIconWidget.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);

        if(idPreview != null) {
            GuiHelper.renderScalableText(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    Component.translatable("visor.overlay.options.overlays.id", idPreview.get()).getString(),
                    ID_COLOR.asInt(),
                    startX + 18, startY + 58,
                    84, 5,
                    false
            );
        }
    }

    @Override
    public void onTick() {
        McGuiUtils.tickEditBox(nameWidget);
        descriptionWidget.tick();
        setupIconWidget.onTick();
    }




}

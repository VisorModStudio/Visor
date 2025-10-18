package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionTextures;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.widgets.ButtonImaged;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public class OptionsScreenGlobal extends OptionsScreen<OverlayOptionsGlobal> {



    public OptionsScreenGlobal(@NotNull OverlayOptionsGlobal optionCategory) {
        super(optionCategory, Background.VERTICAL);
    }

    @Override
    protected void onInit() {
        clearWidgets();

        var text = Component.translatable(
                "visor.overlay.options.global.update_options",
                optionCategory.getOptionsUpdaterType().getName().getString()
        );
        var button = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos((width /2 - 83/2),30)
                        .size(83, 15)
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .highlight(
                                OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT
                        )
                        .setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(text)
                        .setScaleText(true)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.global.update_options.tooltip"))),
                (it) -> {
                    optionCategory.setOptionsUpdaterType(
                            optionCategory.getOptionsUpdaterType().next()
                    );
                    it.setMessage(
                            Component.translatable(
                                    "visor.overlay.options.global.update_options",
                                    optionCategory.getOptionsUpdaterType().getName().getString()
                            )
                    );
                }
        );

        this.addRenderableWidget(
                button
        );
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
}

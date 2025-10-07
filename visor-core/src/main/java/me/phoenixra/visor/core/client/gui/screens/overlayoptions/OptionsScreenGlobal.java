package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionTextures;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.widgets.ImageButton;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButton;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public class OptionsScreenGlobal extends OverlayOptionsScreen<OverlayOptionsGlobal> {



    public OptionsScreenGlobal(@NotNull OverlayOptionsGlobal optionCategory) {
        super(optionCategory);
    }

    @Override
    protected void init() {
        clearWidgets();

        var text = Component.translatable(
                "visor.overlay.options.global.update_options",
                optionCategory.getOptionsUpdaterType().getName().getString()
        );
        var button = new ImageButton(
                new WidgetInfoButton(
                        OverlayOptionTextures.GENERAL_BUTTON,
                        OverlayOptionTextures.GENERAL_BUTTON_HOVERED,
                         (width /2 - 83/2),
                        50,
                        83, 15
                ).setTextColor(VROverlaySettings.TEXT_COLOR)
                        .setText(text)
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
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);

        super.render(guiGraphics, i, j, f);
    }
}

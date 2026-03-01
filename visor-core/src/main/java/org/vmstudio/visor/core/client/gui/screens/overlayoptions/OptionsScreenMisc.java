package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Getter
public class OptionsScreenMisc extends OptionsScreen<OverlayOptionsMisc> {



    public OptionsScreenMisc(@NotNull OverlayOptionsMisc optionsGroup) {
        super(optionsGroup, Background.VERTICAL);
    }

    @Override
    protected void onInit() {

        var text = Component.translatable(
                "visor.overlay.options.misc.update_options",
                optionsGroup.getOptionsUpdaterType().getName().getString()
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
                        .setDynamicTextScale(true)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.misc.update_options.tooltip"))),
                (it) -> {
                    optionsGroup.setOptionsUpdaterType(
                            optionsGroup.getOptionsUpdaterType().next()
                    );
                    it.setMessage(
                            Component.translatable(
                                    "visor.overlay.options.misc.update_options",
                                    optionsGroup.getOptionsUpdaterType().getName().getString()
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

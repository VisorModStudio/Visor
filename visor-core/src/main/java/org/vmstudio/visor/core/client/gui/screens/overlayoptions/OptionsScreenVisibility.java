package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.pose.OptionsPoseTextures;

public class OptionsScreenVisibility extends OptionsScreen<OverlayOptionsVisibility> {



    public OptionsScreenVisibility(@NotNull OverlayOptionsVisibility optionsGroup) {
        super(optionsGroup, OptionsScreen.Background.VERTICAL);
    }

    @Override
    protected void onInit() {

        var button = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+(cursorBoundsWidth-35)/2, cursorBoundsY+35)
                        .size(35,35)
                        .setTexture(OptionsPoseTextures.BUTTON_AIM)
                        .setTextureSelected(OptionsPoseTextures.BUTTON_AIM_ACTIVE)
                        .setInactiveOnSelected(false)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.visibility.tooltip"))),
                (it) ->{
                    optionsGroup.setVisible(!optionsGroup.isVisible());
                    it.setSelected(optionsGroup.isVisible());
                }
        );
        button.setSelected(optionsGroup.isVisible());

        this.addRenderableWidget(
                button
        );
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }
}

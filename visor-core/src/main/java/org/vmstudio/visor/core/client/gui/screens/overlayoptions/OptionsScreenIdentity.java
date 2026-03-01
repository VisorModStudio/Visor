package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.widgets.identity.SetupIdentityWidgetSet;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Getter
public class OptionsScreenIdentity extends OptionsScreen<OverlayOptionsIdentity> {

    private SetupIdentityWidgetSet widgetSet;

    public OptionsScreenIdentity(@NotNull OverlayOptionsIdentity optionsGroup) {
        super(optionsGroup, Background.VERTICAL);
    }

    @Override
    protected void onInit() {
        widgetSet = new SetupIdentityWidgetSet(
                (width - 128) /2,
                (height - 256) /2 - 20,
                false
        );
        widgetSet.initWidgets();

        widgetSet.getNameWidget().setValue(optionsGroup.getRawName());
        widgetSet.getNameWidget().setResponder(optionsGroup::setName);

        widgetSet.getDescriptionWidget().setValue(optionsGroup.getRawDescription());
        widgetSet.getDescriptionWidget().setResponder(optionsGroup::setDescription);

        Consumer<String> responder = (text) -> {

        };
        responder.accept("newText");
        widgetSet.getSetupIconWidget().setIconPath(optionsGroup.getRawIcon());
        widgetSet.getSetupIconWidget().setResponder(optionsGroup::setIcon);

        widgetSet.getWidgets().forEach(this::addRenderableWidget);

    }

    @Override
    public void tick() {
        super.tick();
        widgetSet.onTick();
    }

    @Override
    public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        widgetSet.onPreRender(guiGraphics, mouseX, mouseY,partialTick);
    }

}

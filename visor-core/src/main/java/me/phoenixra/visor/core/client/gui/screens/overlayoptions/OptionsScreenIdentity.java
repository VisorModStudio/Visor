package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets.identity.SetupIdentityWidgetSet;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

@Getter
public class OptionsScreenIdentity extends OptionsScreen<OverlayOptionsIdentity> {

    private SetupIdentityWidgetSet widgetSet;

    private String newName;
    private String newDescription;
    private String newIcon;

    public OptionsScreenIdentity(@NotNull OverlayOptionsIdentity optionCategory) {
        super(optionCategory, Background.VERTICAL);
    }

    @Override
    protected void onInit() {
        widgetSet = new SetupIdentityWidgetSet(
                (width - 128) /2,
                (height - 256) /2 - 20,
                false
        );
        widgetSet.initWidgets();

        widgetSet.getNameWidget().setValue(optionCategory.getRawName());
        widgetSet.getNameWidget().setResponder(optionCategory::setName);

        widgetSet.getDescriptionWidget().setValue(optionCategory.getRawDescription());
        widgetSet.getDescriptionWidget().setResponder(optionCategory::setDescription);

        widgetSet.getSetupIconWidget().setIconPath(optionCategory.getRawIcon());
        widgetSet.getSetupIconWidget().setResponder(optionCategory::setIcon);

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

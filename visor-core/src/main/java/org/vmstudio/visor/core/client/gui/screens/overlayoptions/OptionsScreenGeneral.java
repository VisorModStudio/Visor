package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsGeneral;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.Property;
import org.vmstudio.visor.api.client.gui.widgets.sets.WidgetsList;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;


public class OptionsScreenGeneral extends OptionsScreen<OverlayOptionsGeneral> {


    private WidgetsList widgetsList;

    public OptionsScreenGeneral(@NotNull OverlayOptionsGeneral optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        widgetsList =  new WidgetsList.Builder(
                this::repopulateWidgets,
                optionsGroup.getPropertyList().stream().map(
                        Property::createWidget
                ).toList()
        ).pos(cursorBoundsX+10, cursorBoundsY+15)
                .size(cursorBoundsWidth-20, cursorBoundsHeight-30)
                .setColumns(2)
                .setEntryHeight(15)
                .build();

        widgetsList.initWidgets()
                .forEach(this::addRenderableWidget);
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        widgetsList.onPreRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        widgetsList.onPostRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        widgetsList.onTick();
    }

    public void repopulateWidgets() {
        clearWidgets();
        widgetsList.getWidgets().forEach(this::addRenderableWidget);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        widgetsList.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, scrollX, delta);
    }
}

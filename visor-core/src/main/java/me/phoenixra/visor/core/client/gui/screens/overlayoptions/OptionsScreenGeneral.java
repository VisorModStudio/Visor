package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsGeneral;
import me.phoenixra.visor.api.client.gui.overlays.options.types.properties.Property;
import me.phoenixra.visor.api.client.gui.widgets.sets.WidgetsList;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;


public class OptionsScreenGeneral extends OptionsScreen<OverlayOptionsGeneral> {


    private WidgetsList widgetsList;

    public OptionsScreenGeneral(@NotNull OverlayOptionsGeneral optionCategory) {
        super(optionCategory, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        widgetsList =  new WidgetsList.Builder(
                this::repopulateWidgets,
                optionCategory.getPropertyList().stream().map(
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        widgetsList.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}

package me.phoenixra.visor.core.client.gui.screens.overlaysettings.modelview.widgets;

import me.phoenixra.visor.core.client.gui.screens.overlaysettings.OptionsScreenModelView;
import me.phoenixra.visor.core.client.gui.screens.overlaysettings.modelview.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ByOffsetWidgets extends WidgetSet{

    private Button applyOffsetButton;
    public ByOffsetWidgets(OptionsScreenModelView owner) {
        super(ModificationType.BY_OFFSET, owner);
    }
    @Override
    public List<AbstractWidget> getWidgets() {

        return List.of(applyOffsetButton);
    }
    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY, int edgeWidth, int edgeHeight) {
        int middleX = edgeX + edgeWidth/2;

        applyOffsetButton = Button.builder(
                        Component.translatable("visor.overlaySettings.modelView.widget.applyOffset"),
                (p)->{
                    if(!owner.isDemoDisplayed()
                            || owner.getDemoOverlay().isEmulatingModelView()) {
                        return;
                    }
                    owner.getDemoOverlay().applyNewOffset();
                })
                .pos(middleX - 100/2, edgeY + 10)
                .size(100, 15)
                .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.applyOffset.tooltip")))
                .build();

    return List.of(applyOffsetButton);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

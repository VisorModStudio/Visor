package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;

import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ByOffsetWidgets extends WidgetSet{

    private Button applyOffsetButton;
    public ByOffsetWidgets(OptionsScreenPose owner) {
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
                        Component.translatable("visor.overlay.options.pose.apply_offset"),
                (p)->{
                    if(!owner.isDemoDisplayed()
                            || owner.getDemoOverlay().isEmulatingPose()) {
                        return;
                    }
                    owner.getDemoOverlay().applyNewOffset();
                })
                .pos(middleX - 100/2, edgeY + 10)
                .size(100, 15)
                .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.apply_offset.tooltip")))
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

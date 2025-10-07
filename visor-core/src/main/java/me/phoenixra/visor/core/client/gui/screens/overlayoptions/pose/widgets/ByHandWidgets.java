package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;

import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ByHandWidgets extends WidgetSet{

    private Button startMovingButton;

    public ByHandWidgets(OptionsScreenPose owner) {
        super(ModificationType.BY_HAND, owner);
    }

    @Override
    public List<AbstractWidget> getWidgets() {

        return List.of(startMovingButton);
    }
    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                            int edgeWidth, int edgeHeight) {
        int middleX = edgeX + edgeWidth/2;

        startMovingButton = Button.builder(
                        Component.translatable("visor.overlay.options.pose.move_by_hand"),
                        (p)->{
                            if(!owner.isDemoDisplayed()) {
                                return;
                            }
                            owner.getDemoOverlay().startMovingByAnchor();
                        })
                .pos(middleX - 100/2, edgeY + 10)
                .size(100, 15)
                .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.move_by_hand.tooltip")))
                .build();

        return List.of(startMovingButton);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

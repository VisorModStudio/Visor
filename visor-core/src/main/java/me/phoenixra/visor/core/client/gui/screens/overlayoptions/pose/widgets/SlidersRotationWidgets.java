package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;

import me.phoenixra.visor.api.client.gui.overlay.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.widgets.SliderValueWidget;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public class SlidersRotationWidgets extends WidgetSet {

    private String formulaX;
    private String formulaY;
    private String formulaZ;

    private SliderValueWidget sliderRotationX;
    private SliderValueWidget sliderRotationY;
    private SliderValueWidget sliderRotationZ;
    public SlidersRotationWidgets(OptionsScreenPose owner) {
        super(ModificationType.SLIDERS_POSITION, owner);
    }

    @Override
    public List<AbstractWidget> getWidgets() {

        return List.of(sliderRotationX, sliderRotationY, sliderRotationZ);
    }
    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY, int edgeWidth, int edgeHeight) {
        OverlayOptionsPose optionCategory = owner.getOptionCategory();

        formulaX = Objects.requireNonNullElse(
                optionCategory.getFormulaRotationX(),
        "0"
        );
        formulaY = Objects.requireNonNullElse(
                optionCategory.getFormulaRotationY(),
                "0"
        );
        formulaZ = Objects.requireNonNullElse(
                optionCategory.getFormulaRotationZ(),
                "0"
        );

        int middleX = edgeX + edgeWidth/2;

        sliderRotationX = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 10)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.slider_rotation_x")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaRotationX("("+formulaX+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        sliderRotationY = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 30)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.slider_rotation_y")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaRotationY("("+formulaY+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        sliderRotationZ = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 50)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.slider_rotation_z")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaRotationZ("("+formulaZ+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        return List.of(sliderRotationX, sliderRotationY, sliderRotationZ);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

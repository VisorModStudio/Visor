package me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.widgets;

import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.gui.widgets.SliderValueWidget;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenModelView;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

public class SlidersPositionWidgets extends WidgetSet {

    private String formulaX;
    private String formulaY;
    private String formulaZ;

    private SliderValueWidget sliderPosX;
    private SliderValueWidget sliderPosY;
    private SliderValueWidget sliderPosZ;
    public SlidersPositionWidgets(OptionsScreenModelView owner) {
        super(ModificationType.SLIDERS_POSITION, owner);
    }

    @Override
    public List<AbstractWidget> getWidgets() {

        return List.of(sliderPosX,sliderPosY,sliderPosZ);
    }
    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY, int edgeWidth, int edgeHeight) {
        OverlayOptionsModelView optionCategory = owner.getOptionCategory();

        formulaX = Objects.requireNonNullElse(
                optionCategory.getFormulaPosX(),
        "0"
        );
        formulaY = Objects.requireNonNullElse(
                optionCategory.getFormulaPosY(),
                "0"
        );
        formulaZ = Objects.requireNonNullElse(
                optionCategory.getFormulaPosZ(),
                "0"
        );


        int middleX = edgeX + edgeWidth/2;

        sliderPosX = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 10)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.sliderPosX")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaPosX("("+formulaX+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        sliderPosY = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 30)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.sliderPosY")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaPosY("("+formulaY+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        sliderPosZ = SliderValueWidget.builder()
                .pos(middleX - 100/2, edgeY + 50)
                .size(100, 15)
                .setSnapIncrement(0.001f)
                .setTooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.sliderPosZ")))
                .setOnValueChange(it->{
                    optionCategory.setFormulaPosZ("("+formulaZ+") + "+it);
                    optionCategory.update(true);
                })
                .build();

        return List.of(sliderPosX,sliderPosY,sliderPosZ);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

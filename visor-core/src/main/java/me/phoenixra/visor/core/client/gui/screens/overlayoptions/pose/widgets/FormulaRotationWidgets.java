package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;


import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.widgets.WidgetsFactory;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FormulaRotationWidgets extends WidgetSet{
    protected EditBox fieldRotationX;
    protected EditBox fieldRotationY;
    protected EditBox fieldRotationZ;

    public FormulaRotationWidgets(OptionsScreenPose owner){
        super(ModificationType.FORMULA_ROTATION, owner);
    }

    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                            int edgeWidth, int edgeHeight) {

        OverlayOptionsPose optionCategory = owner.getOptionCategory();


        int middleX = edgeX + edgeWidth/2;

        fieldRotationX = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 10,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_rotation_x"),
                optionCategory.getFormulaRotationX(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaRotationX(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldRotationY = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 35,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_rotation_y"),
                optionCategory.getFormulaRotationY(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaRotationY(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldRotationZ = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 60,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_rotation_z"),
                optionCategory.getFormulaRotationZ(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaRotationZ(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        return List.of(fieldRotationX, fieldRotationY, fieldRotationZ);
    }

    @Override
    public List<AbstractWidget> getWidgets() {
        return List.of(fieldRotationX, fieldRotationY, fieldRotationZ);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

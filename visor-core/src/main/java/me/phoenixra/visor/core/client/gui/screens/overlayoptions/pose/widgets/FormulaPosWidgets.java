package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.OptionsScreenPose;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.PoseWidgetsHelper;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FormulaPosWidgets extends PoseWidgetSet {
    protected EditBox fieldPosX;
    protected EditBox fieldPosY;
    protected EditBox fieldPosZ;

    public FormulaPosWidgets(OptionsScreenPose owner){
        super(ModificationType.FORMULA_POSITION, owner);
    }

    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                            int edgeWidth, int edgeHeight) {

        OverlayOptionsPose optionCategory = owner.getOptionCategory();


        int middleX = edgeX + edgeWidth/2;

        fieldPosX = PoseWidgetsHelper.createFormulaEditor(
                middleX - 150/2, edgeY + 10,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_pos_x"),
                optionCategory.getFormulaPosX(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaPosX(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldPosY = PoseWidgetsHelper.createFormulaEditor(
                middleX - 150/2, edgeY + 35,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_pos_y"),
                optionCategory.getFormulaPosY(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaPosY(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldPosZ = PoseWidgetsHelper.createFormulaEditor(
                middleX - 150/2, edgeY + 60,
                150, 15,
                Component.translatable("visor.overlay.options.pose.formula_pos_z"),
                optionCategory.getFormulaPosZ(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaPosZ(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        return List.of(fieldPosX, fieldPosY, fieldPosZ);
    }

    @Override
    public List<AbstractWidget> getWidgets() {
        return List.of(fieldPosX, fieldPosY, fieldPosZ);
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onRender() {

    }
}

package me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.widgets;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.gui.widgets.WidgetsFactory;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenModelView;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.ModificationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FormulaPosWidgets extends WidgetSet{
    protected EditBox fieldPosX;
    protected EditBox fieldPosY;
    protected EditBox fieldPosZ;

    public FormulaPosWidgets(OptionsScreenModelView owner){
        super(ModificationType.FORMULA_POSITION, owner);
    }

    @Override
    public List<AbstractWidget> initWidgets(int edgeX, int edgeY,
                                            int edgeWidth, int edgeHeight) {

        OverlayOptionsModelView optionCategory = owner.getOptionCategory();


        int middleX = edgeX + edgeWidth/2;

        fieldPosX = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 10,
                150, 15,
                Component.translatable("visor.overlaySettings.modelView.widget.formulaPosX"),
                optionCategory.getFormulaPosX(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaPosX(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldPosY = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 35,
                150, 15,
                Component.translatable("visor.overlaySettings.modelView.widget.formulaPosY"),
                optionCategory.getFormulaPosY(),
                AtumColor.WHITE,
                (it) -> {
                    optionCategory.setFormulaPosY(
                            it
                    );
                    optionCategory.update(true);
                }
        );

        fieldPosZ = WidgetsFactory.createFormulaEditor(
                middleX - 150/2, edgeY + 60,
                150, 15,
                Component.translatable("visor.overlaySettings.modelView.widget.formulaPosZ"),
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

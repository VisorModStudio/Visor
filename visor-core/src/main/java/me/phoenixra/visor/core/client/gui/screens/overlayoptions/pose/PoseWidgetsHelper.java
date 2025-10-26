package me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose;


import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class PoseWidgetsHelper {

    public static EditBox createFormulaEditor(int x, int y,
                                              int width, int height,
                                              Component name,
                                              String initialText,
                                              AtumColor textColor,
                                              Consumer<String> responder){
        EditBox formulaEditor = new EditBox(
                Minecraft.getInstance().font,
                x,y,
                width,height,
                Component.empty()
        );

        formulaEditor.setMaxLength(50);
        formulaEditor.setTextColor(textColor.toInt());

        formulaEditor.setResponder(it->{
            try {
                double result = VRMathUtils.getEvaluated(VisorAPI.client().getConfigManager(), it);
                formulaEditor.setTextColor(textColor.toInt());
                formulaEditor.setTooltip(Tooltip.create(
                        Component.literal(name.getString()+"\n\n'"+it+
                                "'\n= " + String.format("%.3f", result)
                        )
                        )
                );
                responder.accept(it);
            }catch (Throwable throwable){
                formulaEditor.setTextColor(AtumColor.RED.toInt());
                formulaEditor.setTooltip(Tooltip.create(
                                Component.literal(name.getString()+"\n\n" + "§cBad formula!")
                        )
                );
            }
        });
        formulaEditor.setValue(initialText);
        return formulaEditor;
    }
}

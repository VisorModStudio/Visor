package me.phoenixra.visor.core.client.settings.option.gui.elements;

import lombok.Getter;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;


public class VRGuiOptionSlider extends AbstractSliderButton implements VRGuiOptionElement {

    @Getter
    private final VRGuiOption guiOptionType;
    private final boolean showOnlyValue;

    public VRGuiOptionSlider(VRGuiOption guiOptionType,
                             int x, int y,
                             int width, int height,
                             boolean showOnlyValue
    ) {
        super(x, y, width, height,
                Component.literal(
                        ClientContext
                                .settingsHandler
                                .getButtonDisplayString(
                                        guiOptionType, showOnlyValue
                                )
                ),
                normalizeValue(
                        guiOptionType,
                        ClientContext.settingsHandler
                                .getGuiOptionSliderValue(guiOptionType)
                )
        );

        this.guiOptionType = guiOptionType;
        this.showOnlyValue = showOnlyValue;
    }

    public VRGuiOptionSlider(VRGuiOption option, int x, int y) {
        this(option, x, y, 150, 20, false);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal(
                        ClientContext.settingsHandler
                                .getButtonDisplayString(
                                this.guiOptionType, showOnlyValue
                        )
                )
        );
    }

    @Override
    protected void applyValue() {
        double result = denormalizeValue(guiOptionType, (float) this.value);
        ClientContext.settingsHandler.setGuiOptionValue(
                this.guiOptionType, (float) result
        );

        InputType inputType = Minecraft.getInstance().getLastInputType();
        if (inputType == InputType.MOUSE) {
            this.value = normalizeValue(guiOptionType, (float) result);
        }
    }


    private static double normalizeValue(VRGuiOption option, float value) {
        return Mth.clamp(
                (snapToStep(option, value) - option.getSliderValueMin())
                / (option.getSliderValueMax() - option.getSliderValueMin()),
                0.0D, 1.0D
        );
    }

    private static double denormalizeValue(VRGuiOption option, float value) {
        return snapToStep(
                option,
                (float) (option.getSliderValueMin()
                        + (option.getSliderValueMax() - option.getSliderValueMin())
                        * Mth.clamp(value, 0.0D, 1.0D)
                )
        );
    }

    private static float snapToStep(VRGuiOption option, float value) {
        if (option.getSliderStep() > 0.0F) {
            value = option.getSliderStep()
                    * (float) Math.round(value / option.getSliderStep());
        }

        return value;
    }
}

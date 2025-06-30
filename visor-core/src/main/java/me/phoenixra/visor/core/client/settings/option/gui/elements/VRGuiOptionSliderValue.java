package me.phoenixra.visor.core.client.settings.option.gui.elements;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.widgets.SliderValueWidget;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class VRGuiOptionSliderValue extends SliderValueWidget
        implements VRGuiOptionElement {

    @Getter
    private final VRGuiOption guiOptionType;


    public VRGuiOptionSliderValue(VRGuiOption guiOptionType,
                                  int x, int y) {
        super(x, y, 150, 20,
                Component.literal(
                        ClientContext
                                .settingsHandler
                                .getButtonDisplayString(
                                        guiOptionType, false
                                )
                ),
                ClientContext.settingsHandler
                        .getGuiOptionSliderValue(guiOptionType),
                Float.MIN_VALUE, Float.MAX_VALUE);
        this.guiOptionType = guiOptionType;
        setOnValueChange(value->{
            ClientContext.settingsHandler.setGuiOptionValue(
                    this.guiOptionType, value
            );
        });
    }

}

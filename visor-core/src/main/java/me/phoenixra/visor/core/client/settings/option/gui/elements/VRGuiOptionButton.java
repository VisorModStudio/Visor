package me.phoenixra.visor.core.client.settings.option.gui.elements;

import lombok.Getter;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class VRGuiOptionButton extends Button implements VRGuiOptionElement {
    @Getter
    @Nullable
    private final VRGuiOption guiOptionType;

    public VRGuiOptionButton(int x, int y,
                             int width, int height,
                             @Nullable VRGuiOption guiOptionType,
                             String text,
                             OnPress onPress) {
        super(
                x, y,
                width, height,
                Component.translatable(text),
                onPress,
                Button.DEFAULT_NARRATION
        );
        this.guiOptionType = guiOptionType;
    }
    public VRGuiOptionButton(int x, int y,
                             @Nullable VRGuiOption guiOptionType,
                             String text,
                             OnPress onPress) {
        this(
                x, y,
                150, 20,
                guiOptionType,
                text,
                onPress
        );
    }
    public VRGuiOptionButton(int x, int y,
                             String text,
                             OnPress onPress) {
        this(
                x, y,
                null,
                text,
                onPress
        );
    }
}

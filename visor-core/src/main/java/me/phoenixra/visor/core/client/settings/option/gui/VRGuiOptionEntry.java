package me.phoenixra.visor.core.client.settings.option.gui;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionButton;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionElement;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionSlider;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionSliderValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;



public class VRGuiOptionEntry {

    @Getter
    @Nullable
    private final VRGuiOption guiOptionType;
    @Getter
    @Nullable
    private final Class<? extends Screen> opensScreen;
    @Getter
    @Nullable
    private final VROverlay opensOverlay;
    @Getter
    @Nullable
    private final Runnable actionOnClick;

    private final VRGuiOptionPosition position;
    private final int row;

    private final String buttonText;


    public VRGuiOptionEntry(@NotNull VRGuiOption option,
                            @NotNull VRGuiOptionPosition pos, int row,
                            @Nullable String buttonText
    ) {
        this.opensScreen = null;
        this.opensOverlay = null;
        this.actionOnClick = null;

        this.guiOptionType = option;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public VRGuiOptionEntry(@NotNull Class<? extends Screen> opensScreen,
                            @NotNull VRGuiOptionPosition pos, int row,
                            @Nullable String buttonText
    ) {
        this.guiOptionType = null;
        this.opensOverlay = null;
        this.actionOnClick = null;

        this.opensScreen = opensScreen;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public VRGuiOptionEntry(@NotNull VROverlay opensOverlay,
                            @NotNull VRGuiOptionPosition pos, int row,
                            @Nullable String buttonText
    ) {
        this.guiOptionType = null;
        this.opensScreen = null;
        this.actionOnClick = null;

        this.opensOverlay = opensOverlay;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public VRGuiOptionEntry(@NotNull Runnable actionOnClick,
                            @NotNull VRGuiOptionPosition pos, int row,
                            @Nullable String buttonText
    ) {
        this.guiOptionType = null;
        this.opensScreen = null;
        this.opensOverlay = null;

        this.actionOnClick = actionOnClick;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }


    public VRGuiOptionElement asGuiElement(Screen forScreen) {
        if (guiOptionType != null) {
            if(guiOptionType.isSpecialSlider()){
                return new VRGuiOptionSliderValue(
                        guiOptionType,
                        getX(forScreen.width),
                        getY(forScreen.height)) {
                    public void onClick(double pMouseX, double pMouseY) {
                        super.onClick(pMouseX, pMouseY);
                    }
                };
            }
            if (guiOptionType.isSliderUsed()) {
                return new VRGuiOptionSlider(
                        guiOptionType,
                        getX(forScreen.width),
                        getY(forScreen.height)) {
                    public void onClick(double pMouseX, double pMouseY) {
                        super.onClick(pMouseX, pMouseY);
                    }
                };
            }
            return new VRGuiOptionButton(
                    getX(forScreen.width),
                    getY(forScreen.height),
                    getGuiOptionType(),
                    getButtonText(),
                    (button) -> {
                        ClientContext.settingsHandler
                                .updateGuiOptionValue(
                                        getGuiOptionType()
                                );
                        button.setMessage(Component.literal(getButtonText()));
                    });
        }

        if (opensScreen != null) {
            return new VRGuiOptionButton(
                    getX(forScreen.width),
                    getY(forScreen.height),
                    getButtonText(),
                    (button) -> {
                        try {

                            ClientContext.settingsHandler.saveOptions();
                            Minecraft.getInstance().setScreen(
                                    opensScreen
                                            .getConstructor(Screen.class)
                                            .newInstance(forScreen)
                            );
                        } catch (ReflectiveOperationException exception) {
                            LoggerUtils.printError(exception);
                        }
                    }
            );
        }

        if (opensOverlay != null) {
            return new VRGuiOptionButton(
                    getX(forScreen.width),
                    getY(forScreen.height),
                    getButtonText(),
                    (button) -> {
                        ClientContext.settingsHandler.saveOptions();

                        opensOverlay.setEnabled(false);
                        opensOverlay.setEnabled(true);
                    }
            );
        }
        return new VRGuiOptionButton(
                getX(forScreen.width),
                getY(forScreen.height),
                getButtonText(),
                (button) -> {actionOnClick.run();}
        );
    }


    public int getX(int screenWidth) {
        if (this.position == VRGuiOptionPosition.LEFT) {
            return screenWidth / 2 - 155;
        }
        if (this.position == VRGuiOptionPosition.RIGHT) {
            return screenWidth / 2 + 5;
        }
        return screenWidth / 2 - 155 + 80;
    }

    public int getY(int screenHeight) {
        return (int) Math.ceil(
                (float) (screenHeight / 6) + 21.0F * this.row - 10.0F
        );
    }

    public String getButtonText() {
        return this.buttonText.isEmpty()
                && this.guiOptionType != null
                ? ClientContext.settingsHandler.getButtonDisplayString(this.guiOptionType)
                : this.buttonText;
    }

    public int getOrdinal() {
        return this.guiOptionType == null ?
                0 : this.guiOptionType.ordinal();
    }
}

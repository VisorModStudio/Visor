package me.phoenixra.visor.core.client.gui.screens.settings;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;



public class OptionWidgetEntry {

    @Getter
    @Nullable
    private final VROptionWidgetType optionType;
    @Getter
    @Nullable
    private final Class<? extends Screen> opensScreen;
    @Getter
    @Nullable
    private final VROverlay opensOverlay;
    @Getter
    @Nullable
    private final Runnable actionOnClick;

    private final OptionWidgetPosition position;
    private final int row;

    private final String buttonText;


    public OptionWidgetEntry(@NotNull VROptionWidgetType option,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.opensScreen = null;
        this.opensOverlay = null;
        this.actionOnClick = null;

        this.optionType = option;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public OptionWidgetEntry(@NotNull Class<? extends Screen> opensScreen,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.optionType = null;
        this.opensOverlay = null;
        this.actionOnClick = null;

        this.opensScreen = opensScreen;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public OptionWidgetEntry(@NotNull VROverlay opensOverlay,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.optionType = null;
        this.opensScreen = null;
        this.actionOnClick = null;

        this.opensOverlay = opensOverlay;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public OptionWidgetEntry(@NotNull Runnable actionOnClick,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.optionType = null;
        this.opensScreen = null;
        this.opensOverlay = null;

        this.actionOnClick = actionOnClick;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }


    public AbstractWidget createWidget(Screen forScreen) {


        if (optionType != null) {
            return optionType.getBehaviour().getWidget(
                    getWidgetX(forScreen.width),
                    getWidgetY(forScreen.height),
                    getWidgetWidth(),
                    getWidgetHeight()
            );
        }

        Button.OnPress onPress = (button) -> actionOnClick.run();

        if (opensScreen != null) {
            onPress = (button) -> {
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
            };
        } else if (opensOverlay != null) {
            onPress = (button) -> {
                ClientContext.settingsHandler.saveOptions();

                opensOverlay.setEnabled(false);
                opensOverlay.setEnabled(true);
            };
        }

        return new Button.Builder(
                Component.translatable(getButtonText()),
                onPress
        ).bounds(
                getWidgetX(forScreen.width),
                getWidgetY(forScreen.height),
                getWidgetWidth(),
                getWidgetHeight()
        ).build();
    }


    public int getWidgetX(int screenWidth) {
        if (this.position == OptionWidgetPosition.LEFT) {
            return screenWidth / 2 - getWidgetWidth() - 5;
        }
        if (this.position == OptionWidgetPosition.RIGHT) {
            return screenWidth / 2 + 5;
        }
        return screenWidth / 2 + 80 - getWidgetWidth() - 5;
    }

    public int getWidgetY(int screenHeight) {
        return (int) Math.ceil(
                (float) (screenHeight / 6) + (getWidgetHeight() + 1) * this.row - 10.0F
        );
    }
    public int getWidgetWidth(){
        return 150;
    }
    public int getWidgetHeight(){
        return 20;
    }

    public String getButtonText() {
        return this.buttonText.isEmpty()
                && this.optionType != null
                ? ClientContext.settingsHandler.getOptionButtonName(this.optionType)
                : this.buttonText;
    }

    public int getOrdinal() {
        return this.optionType == null ?
                0 : this.optionType.ordinal();
    }
}

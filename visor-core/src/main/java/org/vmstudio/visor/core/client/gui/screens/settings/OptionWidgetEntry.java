package org.vmstudio.visor.core.client.gui.screens.settings;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;


public class OptionWidgetEntry {

    private final VROptionsSet owner;

    @Getter
    @Nullable
    private final VROptionWidgetType optionType;

    @Getter
    @Nullable
    private final Class<? extends Screen> opensScreen;

    @Getter
    @Nullable
    private final VROptionsSet opensOptions;

    @Getter
    @Nullable
    private final Runnable onClick;

    private final OptionWidgetPosition position;
    private final int row;

    private final String buttonText;


    public OptionWidgetEntry(@NotNull VROptionsSet owner,
                             @NotNull VROptionWidgetType option,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.owner = owner;

        this.opensScreen = null;
        this.opensOptions = null;
        this.onClick = null;

        this.optionType = option;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public OptionWidgetEntry(@NotNull VROptionsSet owner,
                             @NotNull Class<? extends Screen> opensScreen,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.owner = owner;

        this.optionType = null;
        this.opensOptions = null;
        this.onClick = null;

        this.opensScreen = opensScreen;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }

    public OptionWidgetEntry(@NotNull VROptionsSet owner,
                             @NotNull VROptionsSet opensOptions,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.owner = owner;

        this.optionType = null;
        this.opensScreen = null;
        this.onClick = null;

        this.opensOptions = opensOptions;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }


    public OptionWidgetEntry(@NotNull VROptionsSet owner,
                             @NotNull Runnable onClick,
                             @NotNull OptionWidgetPosition pos, int row,
                             @Nullable String buttonText
    ) {
        this.owner = owner;

        this.optionType = null;
        this.opensScreen = null;
        this.opensOptions = null;

        this.onClick = onClick;
        this.position = pos;
        this.row = row;

        this.buttonText = Objects.requireNonNullElse(
                buttonText, ""
        );
    }


    public AbstractWidget createWidget() {


        if (optionType != null) {
            return optionType.getBehaviour().getWidget(
                    getWidgetX(),
                    getWidgetY(),
                    getWidgetWidth(),
                    getWidgetHeight()
            );
        }

        Consumer<ButtonImaged> onPress = (button) -> this.onClick.run();

        if (opensScreen != null) {
            onPress = (button) -> {
                try {

                    ClientContext.settingsManager.saveOptions();
                    Minecraft.getInstance().setScreen(
                            opensScreen
                                    .getConstructor(Screen.class)
                                    .newInstance(owner.getScreen())
                    );
                } catch (ReflectiveOperationException exception) {
                    LoggerUtils.printError(exception);
                }
            };
        }else if(opensOptions != null){
            onPress = (button) -> {
                ClientContext.settingsManager.saveOptions();
                owner.getScreen().switchOptions(opensOptions);
            };
        }

        return new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(getWidgetX(), getWidgetY())
                        .size(getWidgetWidth(), getWidgetHeight())
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable(getButtonText())),
                onPress
        );
    }


    public int getWidgetX() {
        int startX = owner.getScreen().getOptionsStartX();
        int width = owner.getScreen().getOptionsWidth();
        if (this.position == OptionWidgetPosition.LEFT) {
            return startX + (width / 2 - getWidgetWidth() - 5);
        }
        if (this.position == OptionWidgetPosition.RIGHT) {
            return startX + (width / 2 + 5);
        }
        return startX + (width / 2 + 17 - getWidgetWidth() - 5);
    }

    public int getWidgetY() {
        return 7 + owner.getScreen().getOptionsStartY()
                + (getWidgetHeight() + 4) * this.row;
    }
    public int getWidgetWidth(){
        return owner.getScreen().getScaleHelper().scaledSize(52);
    }
    public int getWidgetHeight(){
        return owner.getScreen().getScaleHelper().scaledSize(10);
    }

    public String getButtonText() {
        return this.buttonText.isEmpty()
                && this.optionType != null
                ? ClientContext.settingsManager.getOptionButtonName(this.optionType)
                : this.buttonText;
    }

    public int getOrdinal() {
        return this.optionType == null ?
                0 : this.optionType.ordinal();
    }


}

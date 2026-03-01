package org.vmstudio.visor.core.client.settings.options;

import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.utils.LangHelper;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class OptionBehaviourFactory {


    public static OptionBehaviourBuilder simple(@NotNull VROptionWidgetType optionWidget) {

        return new OptionBehaviourBuilder((entry) -> new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(entry.x, entry.y)
                        .size(entry.z, entry.w)
                        .setTextScale(VRClientSettings.getSettingsTextScale())
                        .setTexture(OptionTextures.GRAY_TEXTURE)
                        .setHighlightEnabled(true)
                        .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                        .setHighlightSelected(OptionTextures.SELECTED_HIGHLIGHT)
                        .setTooltip(getTooltip(optionWidget))
                        .setText(Component.literal(
                                ClientContext.settingsManager.getOptionButtonName(
                                optionWidget
                                )
                        )),
                (button) -> {
                    ClientContext.settingsManager
                            .nextOptionValue(
                                    optionWidget.getKey()
                            );
                    button.setMessage(Component.literal(
                            ClientContext.settingsManager.getOptionButtonName(
                                    optionWidget
                            ))
                    );
                }));
    }

    public static <T> OptionBehaviourBuilder discreteSlider(@NotNull VROptionWidgetType optionWidget,
                                                            List<T> entries,
                                                            Supplier<Integer> indexSupplier) {

        return new OptionBehaviourBuilder((entry) -> {
            var widget = new SliderWidget<>(
                    new WidgetInfoSlider()
                            .pos(entry.x, entry.y)
                            .size(entry.z, entry.w)
                            .setTextScale(VRClientSettings.getSettingsTextScale())
                            .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                            .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE)
                            .highlight(OptionTextures.HOVERED_HIGHLIGHT)
                            .setTooltip(getTooltip(optionWidget)),
                    entries,
                    (it) -> {
                        var selected = it.getSelected();
                        ClientContext.settingsManager
                                .setOptionValue(
                                        optionWidget.getKey(),
                                        selected
                                );
                        it.setText(Component.literal(
                                ClientContext.settingsManager.getOptionButtonName(
                                        optionWidget
                                ))
                        );
                    }
            );
            widget.setSelectedIndex(indexSupplier.get(), false);
            widget.setText(Component.literal(
                    ClientContext.settingsManager.getOptionButtonName(
                            optionWidget
                    ))
            );
            return widget;
        });
    }

    private static Tooltip getTooltip(@NotNull VROptionWidgetType optionWidget){
        String tooltipLang = "visor.options."
                + optionWidget.getKey() + ".tooltip";

        if(!LangHelper.existsLangKey(tooltipLang)){
            return null;
        }

        String tooltip = LangHelper.getText(tooltipLang, (Object) null)
                .replace("\n", "§r\n");
        return Tooltip.create(Component.literal(tooltip));
    }
}

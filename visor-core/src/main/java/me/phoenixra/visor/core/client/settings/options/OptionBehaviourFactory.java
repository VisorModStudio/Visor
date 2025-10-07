package me.phoenixra.visor.core.client.settings.options;

import me.phoenixra.visor.api.client.gui.widgets.DiscreteSliderWidget;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class OptionBehaviourFactory {

    public static OptionBehaviourBuilder actionButton(@NotNull Button.OnPress onPress, Component text) {
        return new OptionBehaviourBuilder((entry) -> new Button.Builder(
                text,
                onPress
        ).pos(entry.x, entry.y)
                .size(entry.z, entry.w)

                .build());
    }

    public static OptionBehaviourBuilder simple(@NotNull VROptionWidgetType optionWidget) {
        return new OptionBehaviourBuilder((entry) -> new Button.Builder(
                Component.literal(
                        ClientContext.settingsHandler.getOptionButtonName(
                                optionWidget
                        )),
                (button) -> {
                    ClientContext.settingsHandler
                            .nextOptionValue(
                                    optionWidget.getKey()
                            );
                    button.setMessage(Component.literal(
                            ClientContext.settingsHandler.getOptionButtonName(
                                    optionWidget
                            ))
                    );
                })
                .pos(entry.x, entry.y)
                .size(entry.z, entry.w)
                .build());
    }

    public static <T> OptionBehaviourBuilder discreteSlider(@NotNull VROptionWidgetType optionWidget,
                                                            List<T> entries,
                                                            Supplier<Integer> indexSupplier) {
        return new OptionBehaviourBuilder((entry) -> {
            var widget = new DiscreteSliderWidget<T>(
                    entry.x, entry.y,
                    entry.z, entry.w,
                    entries, indexSupplier.get(),
                    (it) -> {
                        var widget1 = it.first();
                        var selected = it.second();
                        ClientContext.settingsHandler
                                .setOptionValue(
                                        optionWidget.getKey(),
                                        selected
                                );
                        widget1.setMessage(Component.literal(
                                ClientContext.settingsHandler.getOptionButtonName(
                                        optionWidget
                                ))
                        );
                    }
            );
            widget.setMessage(Component.literal(
                    ClientContext.settingsHandler.getOptionButtonName(
                            optionWidget
                    ))
            );
            return widget;
        });
    }
}

package me.phoenixra.visor.api.client.gui.overlays.options.types.properties;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.client.gui.widgets.EditBoxImage;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;

@Getter
public class PropertyInt extends Property<Integer> {
    protected final int minValue;
    protected final int maxValue;
    protected final WidgetInfoEditBox widgetInfo;

    public PropertyInt(@NotNull String key,
                       int defaultValue,
                       int minValue,
                       int maxValue,
                       @NotNull WidgetInfoEditBox widgetInfo) {
        super(key, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.widgetInfo = widgetInfo;

    }

    @Override
    public void onLoad(@NotNull Config config) {
        setValue(config.getIntOrDefault(key, defaultValue));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue());
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Math.max(minValue, Math.min(maxValue, value)));
    }

    @Override
    public EditBoxImage createWidget() {
        WidgetInfoEditBox widgetInfo = new WidgetInfoEditBox(
                this.widgetInfo
        );
        var widget = new EditBoxImage(widgetInfo);


        widget.setValue(Integer.toString(getValue()));

        widget.setFilter(s -> {
            if (s.isEmpty() || s.equals("-")) return true;
            try {
                int v = Integer.parseInt(s);
                return v >= minValue && v <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        widget.setResponder(s -> {
            if (s.isEmpty() || s.equals("-")) return;
            try {
                setValue(Integer.parseInt(s));
                onValueChanged();
            } catch (NumberFormatException ignored) { }
        });

        int maxDigits = Math.max(
                Integer.toString(Math.abs(minValue)).length(),
                Integer.toString(Math.abs(maxValue)).length()
        ) + (minValue < 0 ? 1 : 0);
        widget.setMaxLength(maxDigits);

        widget.moveCursorToStart();

        return widget;
    }
}

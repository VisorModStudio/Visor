package me.phoenixra.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.client.gui.widgets.EditBoxImage;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;

public class PropertyDouble extends Property<Double> {
    protected final double minValue;
    protected final double maxValue;
    protected final WidgetInfoEditBox widgetInfo;

    public PropertyDouble(@NotNull String key,
                          double defaultValue,
                          double minValue,
                          double maxValue,
                          @NotNull WidgetInfoEditBox widgetInfo) {
        super(key, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.widgetInfo = widgetInfo;
    }

    @Override
    public void onLoad(@NotNull Config config) {
        setValue(config.getDoubleOrDefault(key, defaultValue));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue());
    }

    @Override
    public void setValue(Double value) {
        super.setValue(
                Math.max(minValue, Math.min(maxValue, value))
        );
    }

    @Override
    public EditBoxImage createWidget() {
        WidgetInfoEditBox widgetInfo = new WidgetInfoEditBox(
                this.widgetInfo
        );
        var widget = new EditBoxImage(widgetInfo);

        widget.setValue(formatDouble(getValue()));

        widget.setFilter(s -> {
            if (s.isEmpty() || s.equals("-") || s.equals(".") || s.equals("-.")) return true;
            try {
                double v = Double.parseDouble(s);
                return Double.isFinite(v) && v >= minValue && v <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        widget.setResponder(s -> {
            try {
                double v = Double.parseDouble(s);
                if (Double.isFinite(v) && v >= minValue && v <= maxValue) {
                    setValue(v);
                    onValueChanged();
                }
            } catch (NumberFormatException ignored) {

            }
        });

        int intPartWidth = Math.max(intPartWidth(minValue), intPartWidth(maxValue));
        int maxLen = intPartWidth + 1 /* '.' */ + 15 /* decimals */ + (minValue < 0 ? 1 : 0);
        widget.setMaxLength(Math.max(maxLen, 8));

        widget.moveCursorToStart();

        return widget;
    }

    private static int intPartWidth(double v) {
        double absFloor = Math.floor(Math.abs(v));
        String s = java.math.BigDecimal.valueOf(absFloor).toPlainString();
        return Math.max(1, s.length());
    }

    private static String formatDouble(double v) {
        String s = String.format(java.util.Locale.ROOT, "%.12f", v);
        int dot = s.indexOf('.');
        if (dot >= 0) {
            int end = s.length();
            while (end > dot + 1 && s.charAt(end - 1) == '0') end--;
            if (end == dot + 1) end = dot;
            s = s.substring(0, end);
        }
        return s;
    }
}

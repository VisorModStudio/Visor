package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class PropertyFloat extends Property<Float> {
    protected final float minValue;
    protected final float maxValue;
    protected final WidgetInfoEditBox widgetInfo;

    public PropertyFloat(@NotNull String key,
                         float defaultValue,
                         float minValue,
                         float maxValue,
                         @NotNull WidgetInfoEditBox widgetInfo) {
        super(key, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.widgetInfo = widgetInfo;
    }

    @Override
    public void onLoad(@NotNull Config config) {
        setValue(config.getFloatOrDefault(key, defaultValue));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue());
    }

    @Override
    public void setValue(Float value) {
        super.setValue(
                Mth.clamp(value, minValue, maxValue)
        );
    }

    @Override
    public EditBoxImaged createWidget() {
        WidgetInfoEditBox widgetInfo = new WidgetInfoEditBox(
                this.widgetInfo
        );
        var widget = new EditBoxImaged(widgetInfo);

        widget.setValue(formatFloat(getValue()));

        widget.setFilter(s -> {
            if (s.isEmpty()) return true;
            if (minValue < 0f && (s.equals("-") || s.equals("-."))) return true;
            if (s.equals(".")) return true;

            try {
                float v = Float.parseFloat(s);
                return Float.isFinite(v) && v >= minValue && v <= maxValue;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        widget.setResponder(s -> {
            try {
                float v = Float.parseFloat(s);
                if (Float.isFinite(v) && v >= minValue && v <= maxValue) {
                    setValue(v);
                    onValueChanged();
                }
            } catch (NumberFormatException ignored) {

            }
        });

        int intPartWidth = Math.max(
                Integer.toString((int)Math.floor(Math.abs(minValue))).length(),
                Integer.toString((int)Math.floor(Math.abs(maxValue))).length()
        );
        int maxLen = intPartWidth + 1 /* '.' */ + 8 /* decimals */ + (minValue < 0 ? 1 : 0);
        widget.setMaxLength(Math.max(maxLen, 6));

        widget.moveCursorToStart(false);

        return widget;
    }

    private static String formatFloat(float v) {
        String s = String.format(java.util.Locale.ROOT, "%.6f", v);
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

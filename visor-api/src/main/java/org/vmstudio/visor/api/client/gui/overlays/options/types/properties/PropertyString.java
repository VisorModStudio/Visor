package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.client.gui.widgets.EditBoxImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class PropertyString extends Property<String> {
    protected final int maxLength;
    protected final @Nullable Pattern validator;    // optional full-match pattern (e.g., "^[a-z0-9_\\-]+$")
    protected final WidgetInfoEditBox widgetInfo;

    public PropertyString(@NotNull String key,
                          @NotNull String defaultValue,
                          int maxLength,
                          @Nullable Pattern validator,
                          @NotNull WidgetInfoEditBox widgetInfo) {
        super(key, defaultValue);
        if (maxLength < 1) throw new IllegalArgumentException("maxLength must be >= 1");
        this.maxLength = maxLength;
        this.validator = validator;
        this.widgetInfo = widgetInfo;
    }

    @Override
    public void onLoad(@NotNull Config config) {
        setValue(config.getStringOrDefault(key, defaultValue));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue());
    }

    @Override
    public void setValue(String value) {
        super.setValue(sanitizeForState(value));
    }

    @Override
    public EditBoxImaged createWidget() {
        WidgetInfoEditBox widgetInfo = new WidgetInfoEditBox(
                this.widgetInfo
        );
        var widget = new EditBoxImaged(widgetInfo);

        widget.setValue(getValue());

        widget.setMaxLength(maxLength);
        widget.setFilter(s -> s.length() <= maxLength);

        widget.setResponder(s -> {
            if (isValid(s)) {
                setValue(s);
                onValueChanged();
            }
        });

        widget.moveCursorToStart(false);

        return widget;
    }

    // --- helpers ---

    private boolean isValid(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len > maxLength) return false;
        return validator == null || validator.matcher(s).matches();
    }

    private static boolean fits(String s, int min, int max, @Nullable Pattern re, boolean trim) {
        if (s == null) return false;
        String t = trim ? s.strip() : s;
        int len = t.length();
        if (len < min || len > max) return false;
        return re == null || re.matcher(t).matches();
    }

    private String sanitizeForState(String s) {
        if (s == null) s = "";

        if (s.length() > maxLength) s = s.substring(0, maxLength);

        if (validator != null && !validator.matcher(s).matches()) {
            String d = defaultValue;
            return d.length() > maxLength ? d.substring(0, maxLength) : d;
        }

        return s;
    }
}

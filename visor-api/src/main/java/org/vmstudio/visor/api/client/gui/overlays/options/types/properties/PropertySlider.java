package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class PropertySlider<T> extends Property<T> {
    private final List<T> entries;
    private final WidgetInfoSlider widgetInfo;
    private final @NotNull Function<T, Component> labeler;


    public PropertySlider(@NotNull String key,
                          @NotNull T defaultValue,
                          @NotNull List<T> entries,
                          @NotNull WidgetInfoSlider widgetInfo) {
        this(key, defaultValue, entries, (it)->Component.literal(it.toString()), widgetInfo);
    }


    public PropertySlider(@NotNull String key,
                          @NotNull T defaultValue,
                          @NotNull List<T> entries,
                          @NotNull Function<T, Component> labeler,
                          @NotNull WidgetInfoSlider widgetInfo) {
        super(key, defaultValue);
        this.entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        if (this.entries.isEmpty()) {
            throw new IllegalArgumentException("entries list must not be empty");
        }
        this.widgetInfo = new WidgetInfoSlider(Objects.requireNonNull(widgetInfo, "widgetInfo"));
        this.labeler = Objects.requireNonNull(labeler, "labeler");

        // Ensure stored value is part of entries; if not, fall back to first
        T initial = this.entries.contains(defaultValue) ? defaultValue : this.entries.get(0);
        super.setValue(initial);
    }

    @Override
    public void onLoad(@NotNull Config config) {
        int defaultIdx = indexOf(getDefaultValue());
        int idx = config.getIntOrDefault(key, defaultIdx);
        idx = clampIndex(idx);
        super.setValue(entries.get(idx));
    }

    @Override
    public void onSave(@NotNull Config config) {
        int idx = indexOf(getValue());
        config.set(key, idx);
    }

    @Override
    public void setValue(T value) {
        // Only allow values that exist in entries; otherwise snap to first
        if (entries.contains(value)) {
            super.setValue(value);
        } else {
            super.setValue(entries.get(0));
        }

    }

    @Override
    public SliderWidget<T> createWidget() {
        WidgetInfoSlider info = new WidgetInfoSlider(this.widgetInfo);

        var slider = new SliderWidget<>(
                info,
                entries,
                (it) -> {
                    // Update property value and the displayed label
                    T v = it.getSelected();
                    setValue(v);
                    onValueChanged();
                    it.setText(labeler.apply(v));
                }
        );

        // Initialize to current value (sanitized)
        T current = entries.contains(getValue()) ? getValue() : entries.get(0);
        setValue(current);
        slider.setSelected(current, false);
        slider.setText(labeler.apply(current));

        return slider;
    }

    private int indexOf(T v) {
        int i = entries.indexOf(v);
        return Math.max(i, 0);
    }

    private int clampIndex(int i) {
        if (entries.isEmpty()) return 0;
        if (i < 0) return 0;
        int max = entries.size() - 1;
        return Math.min(i, max);
    }
}
package org.vmstudio.visor.api.client.gui.overlays.options.types.properties;

import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class PropertyEnum <E extends Enum<E>> extends Property<E> {
    private final Class<E> enumClass;
    private final WidgetInfoButtonImaged widgetInfo;
    private final List<E> values;
    private final @NotNull Function<E, Component> labeler;

    public PropertyEnum(@NotNull String key,
                        @NotNull E defaultValue,
                        @NotNull Class<E> enumClass,
                        @NotNull WidgetInfoButtonImaged widgetInfo) {
        this(key, defaultValue, enumClass, e -> Component.literal(humanize(e.name())), widgetInfo);
    }

    public PropertyEnum(@NotNull String key,
                        @NotNull E defaultValue,
                        @NotNull Class<E> enumClass,
                        @NotNull Function<E, Component> labeler,
                        @NotNull WidgetInfoButtonImaged widgetInfo) {
        super(key, defaultValue);
        this.enumClass = Objects.requireNonNull(enumClass, "enumClass");
        this.widgetInfo = Objects.requireNonNull(widgetInfo, "infoButton");
        this.labeler = Objects.requireNonNull(labeler, "labeller");
        this.values = List.of(enumClass.getEnumConstants());
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Enum " + enumClass.getName() + " has no constants");
        }
    }

    @Override
    public void onLoad(@NotNull Config config) {
        String raw = config.getStringOrDefault(key, getDefaultValue().name());
        E parsed = tryParseEnum(raw);
        if (parsed == null) {
            int ordinal = config.getIntOrDefault(key, getDefaultValue().ordinal());
            parsed = (ordinal >= 0 && ordinal < values.size()) ? values.get(ordinal) : getDefaultValue();
        }
        setValue(values.contains(parsed) ? parsed : values.get(0));
    }

    @Override
    public void onSave(@NotNull Config config) {
        config.set(key, getValue().name());
    }

    @Override
    public ButtonImaged createWidget() {
        WidgetInfoButtonImaged widgetInfo
                = new WidgetInfoButtonImaged(this.widgetInfo);

        var button = new ButtonImaged(widgetInfo, imgBtn -> {
            int i = Math.max(0, values.indexOf(getValue()));
            E next = values.get((i + 1) % values.size());
            setValue(next);
            onValueChanged();
            imgBtn.setMessage(labeler.apply(next));
            imgBtn.setSelected(false);
        });

        E current = values.contains(getValue()) ? getValue() : values.get(0);
        setValue(current);
        button.setMessage(labeler.apply(current));
        button.setSelected(false);

        return button;
    }


    private E tryParseEnum(String s) {
        if (s == null) return null;
        try {
            return Enum.valueOf(enumClass, s);
        } catch (IllegalArgumentException e) {
            for (E e2 : enumClass.getEnumConstants()) {
                if (e2.name().equalsIgnoreCase(s)) return e2;
            }
            return null;
        }
    }

    private static String humanize(String name) {
        String[] parts = name.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}

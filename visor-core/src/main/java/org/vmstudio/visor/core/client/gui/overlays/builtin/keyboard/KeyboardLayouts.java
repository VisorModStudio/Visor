package org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.vmstudio.visor.api.client.settings.VRClientSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


public final class KeyboardLayouts {

    private static final Map<KeyboardLayout, KeyboardLayoutKeys> LAYOUT_KEYS = new EnumMap<>(
            KeyboardLayout.class
    );

    static {
        for(var layout : KeyboardLayout.values()){
            register(build(layout));
        }
    }

    private KeyboardLayouts() {
    }

    public static @NotNull KeyboardLayoutKeys get(@NotNull KeyboardLayout layout) {
        KeyboardLayoutKeys layoutKeys = LAYOUT_KEYS.get(layout);
        if (layoutKeys == null) {
            throw new IllegalArgumentException("Unknown keyboard layout: " + layout);
        }
        return layoutKeys;
    }

    public static @NotNull KeyboardLayoutKeys getDefault() {
        return get(KeyboardLayout.ENGLISH);
    }

    public static @NotNull List<KeyboardLayout> getSelectableLayouts() {
        return List.of(KeyboardLayout.values());
    }

    public static @NotNull List<KeyboardLayout> getSelected() {
        return deserialize(VRClientSettings.getKeyboardLayoutsRaw());
    }

    public static void setSelected(@NotNull Collection<KeyboardLayout> layouts) {
        VRClientSettings.setKeyboardLayoutsRaw(serialize(layouts));
    }

    /**
     * Includes auto keyboard layout if supported
     */
    public static @NotNull List<KeyboardLayout> getEffectiveSelected() {
        List<KeyboardLayout> base = getSelected();
        KeyboardLayout autoLayout = getAutoLayout();
        if (autoLayout == null || base.contains(autoLayout)) {
            return base;
        }
        var merged = new ArrayList<>(base);
        if(merged.isEmpty()){
            merged.add(KeyboardLayout.ENGLISH);
        }
        merged.add(autoLayout);
        return List.copyOf(merged);
    }

    public static @Nullable KeyboardLayout getAutoLayout() {
        if (!VRClientSettings.isKeyboardAutoLayout()) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        String langCode = mc.options.languageCode;
        return KeyboardLayout.fromLangCode(langCode);
    }

    public static @NotNull List<KeyboardLayout> deserialize(
            @Nullable String rawValue
    ) {
        LinkedHashSet<KeyboardLayout> result = new LinkedHashSet<>();
        if (rawValue != null && !rawValue.isBlank()) {
            for (String part : rawValue.split(",")) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    KeyboardLayout layout = KeyboardLayout.valueOf(trimmed.toUpperCase());
                    result.add(layout);
                } catch (IllegalArgumentException e) {
                    //empty
                }
            }
        }

        return List.copyOf(result);
    }

    public static @NotNull String serialize(
            @NotNull Iterable<KeyboardLayout> layouts
    ) {
        LinkedHashSet<KeyboardLayout> normalized = new LinkedHashSet<>();
        for (KeyboardLayout layout : layouts) {
            if (layout != null) {
                normalized.add(layout);
            }
        }

        StringJoiner joiner = new StringJoiner(",");
        for (KeyboardLayout layout : normalized) {
            joiner.add(layout.name());
        }
        return joiner.toString();
    }

    private static void register(@NotNull KeyboardLayoutKeys layoutKeys) {
        LAYOUT_KEYS.put(layoutKeys.getLayout(), layoutKeys);
    }

    private static @NotNull KeyboardLayoutKeys build(@NotNull KeyboardLayout layout) {
        KeyboardRow[] rows = layout.getRows();

        KeyboardKey[][] normalLayer = new KeyboardKey[rows.length][];
        KeyboardKey[][] shiftLayer = new KeyboardKey[rows.length][];

        for (int row = 0; row < rows.length; row++) {
            normalLayer[row] = buildRow(rows[row], false, 0);
            shiftLayer[row] = buildRow(rows[row], true, GLFW.GLFW_MOD_SHIFT);
        }

        return new KeyboardLayoutKeys(layout, normalLayer, shiftLayer);
    }

    private static @NotNull KeyboardKey[] buildRow(@NotNull KeyboardRow row,
                                                   boolean shifted,
                                                   int fallbackModifiers) {
        KeyboardKey[] keys = new KeyboardKey[row.size()];
        for (int i = 0; i < keys.length; i++) {
            String symbol = row.symbol(i, shifted);
            keys[i] = new KeyboardKey(
                    symbol,
                    symbol,
                    row.keyCodeAt(i),
                    fallbackModifiers
            );
        }
        return keys;
    }
}

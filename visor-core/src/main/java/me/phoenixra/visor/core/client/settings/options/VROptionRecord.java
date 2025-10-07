package me.phoenixra.visor.core.client.settings.options;

import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public record VROptionRecord(@NotNull Field field,
                             @NotNull VROptionWidgetType widgetType,
                             @NotNull String key) {
}


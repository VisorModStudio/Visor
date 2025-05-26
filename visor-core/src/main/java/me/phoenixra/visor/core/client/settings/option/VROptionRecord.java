package me.phoenixra.visor.core.client.settings.option;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public record VROptionRecord(@NotNull Field field,
                             @NotNull VRGuiOption guiOptionType,
                             @NotNull String key) {
}


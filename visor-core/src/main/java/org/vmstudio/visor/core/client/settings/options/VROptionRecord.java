package org.vmstudio.visor.core.client.settings.options;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public record VROptionRecord(@NotNull Field field,
                             @NotNull String key,
                             boolean excludeForcedChange) {
}

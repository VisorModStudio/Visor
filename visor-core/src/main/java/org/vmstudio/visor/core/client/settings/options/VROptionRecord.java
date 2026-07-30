package org.vmstudio.visor.core.client.settings.options;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.lang.reflect.Field;

public record VROptionRecord(@NotNull Field field,
                             @NotNull String key,
                             boolean excludeForcedChange) {

    public boolean serverOption() {
        return field.getDeclaringClass() == VRServerSettings.class;
    }
}

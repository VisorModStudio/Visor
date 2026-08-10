package org.vmstudio.visor.compatibility.immediatelyfast;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.LoggerUtils;

//IMMEDIATELY FAST COMPATIBILITY CLASS
public final class ImmediatelyFastCompatHelper {
    public static final String MOD_ID = "immediatelyfast";
    private ImmediatelyFastCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static void prepare(@NotNull VisorAddon owner) {
        if (!isLoaded()) {
            return;
        }
        LoggerUtils.getLogger().info("Visor: ImmediatelyFast detected");

        if (errorCheckingDisabled()) {
            LoggerUtils.getLogger().warn("Visor: ImmediatelyFast has 'experimental_disable_error_checking' enabled");
        }
    }

    private static boolean errorCheckingDisabled() {
        try {
            Class<?> immediatelyFast = Class.forName("net.raphimc.immediatelyfast.ImmediatelyFast");
            Object config = immediatelyFast.getField("config").get(null);
            if (config == null) {
                return false;
            }
            return config.getClass()
                    .getField("experimental_disable_error_checking")
                    .getBoolean(config);
        } catch (Throwable t) {
            return false;
        }
    }
}

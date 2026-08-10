package org.vmstudio.visor.core.client.settings;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.common.ServerConfig;

import java.nio.file.Path;

/**
 * Client-side access to server_settings.yml for editing
 * world settings from the main menu.
 */
public final class ServerSettingsFileStore {
    private static ConfigFile config;

    private ServerSettingsFileStore() {
    }

    public static void reloadIntoStatics() {
        ConfigFile file = getOrCreate();
        try {
            file.reload();
        } catch (Exception e) {
            LoggerUtils.printError(e);
        }
        VRServerSettings.resetToDefaults();
        ServerConfig.updateSettings(file);
    }

    public static void save() {
        ConfigFile file = getOrCreate();
        ServerConfig.applySettingsTo(file);
        try {
            file.save();
        } catch (Exception e) {
            LoggerUtils.printError(e);
        }
    }

    private static ConfigFile getOrCreate() {
        if (config != null) {
            return config;
        }
        try {
            config = ClientContext.visor.getConfigManager().createConfigFile(
                    ConfigType.YAML,
                    "server_settings",
                    Path.of("server_settings.yml"),
                    false
            );
        } catch (Exception e) {
            throw new VRException(e);
        }
        return config;
    }
}

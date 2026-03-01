package org.vmstudio.visor.core.common;


import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.server.SendSettingToClient;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.lang.reflect.Field;
import java.nio.file.Path;

public class ServerConfig{
    protected static ServerConfig INSTANCE;
    private ConfigFile config;


    private Config configForClients;

    public ServerConfig(){
        INSTANCE = this;
    }


    public void onServerInit() throws Throwable{
        config = VisorAPI.server().
                getConfigManager().createConfigFile(
                        ConfigType.YAML,
                "server_settings",
                        Path.of("server_settings.yml"),
                false
        );
        updateSettings(config);
    }

    public static void updateSettings(ConfigManager configManager,
                                      String configString){
        Config config = configManager.createConfigFromString(
                ConfigType.YAML,
                configString
        );
        updateSettings(config);
    }
    public static void updateSettings(Config config){
        try {
            Class<VRServerSettings> clazz = VRServerSettings.class;
            for (Field field : clazz.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                    continue;
                }

                String fieldName = field.getName();
                Object value = config.get(fieldName);
                if(value==null) continue;

                field.setAccessible(true);

                // Handle enum types
                if (field.getType().isEnum()) {
                    Class<? extends Enum> enumType = (Class<? extends Enum>)
                            field.getType();
                    Object enumValue = Enum.valueOf(enumType, value.toString());
                    field.set(null, enumValue);
                }

                // Cast the value to the correct type before setting it
                if (field.getType().isAssignableFrom(value.getClass())) {
                    field.set(null, value);
                } else if (field.getType() == boolean.class && value instanceof Boolean) {
                    field.setBoolean(null, (Boolean) value);
                } else if (field.getType() == byte.class && value instanceof Number) {
                    field.setLong(null, ((Number) value).byteValue());
                } else if (field.getType() == short.class && value instanceof Number) {
                    field.setLong(null, ((Number) value).shortValue());
                } else if (field.getType() == int.class && value instanceof Number) {
                    field.setLong(null, ((Number) value).intValue());
                } else if (field.getType() == long.class && value instanceof Number) {
                    field.setLong(null, ((Number) value).longValue());
                } else if (field.getType() == float.class && value instanceof Number) {
                    field.setDouble(null, ((Number) value).floatValue());
                } else if (field.getType() == double.class && value instanceof Number) {
                    field.setDouble(null, ((Number) value).doubleValue());
                }
            }
        }catch (Exception e){
            throw new VRException(e);
        }
    }

    public static Config getSettingsForClient(){
        if(INSTANCE.configForClients != null){
            return INSTANCE.configForClients;
        }
        try {
            INSTANCE.configForClients = VisorAPI.server().getConfigManager().createConfig(
                    ConfigType.YAML,
                    null
            );
            Class<VRServerSettings> clazz = VRServerSettings.class;
            for (Field field : clazz.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                field.setAccessible(true);
                if(!field.isAnnotationPresent(SendSettingToClient.class)){
                    continue;
                }

                String fieldName = field.getName();
                Object value = field.get(null);
                if(value==null) continue;
                if (field.getType().isEnum()) {
                    value = value.toString();
                }
                INSTANCE.configForClients.set(fieldName, value);
            }
            return INSTANCE.configForClients;
        }catch (Exception e){
            throw new VRException(e);
        }
    }

}

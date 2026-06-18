package org.vmstudio.visor.core.client.settings;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.config.catalog.ConfigCatalog;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.misc.color.AtumColorMutable;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.client.settings.options.VROptionField;
import org.vmstudio.visor.core.client.settings.options.VROptionRecord;
import org.vmstudio.visor.core.client.settings.overlays.OverlayConfigsManager;
import org.vmstudio.visor.core.client.settings.presets.PresetsCatalogListener;
import org.vmstudio.visor.core.client.settings.presets.VRPresetSettingsTypeImpl;
import org.vmstudio.visor.core.client.settings.presets.VRSettingsPresetRegistry;
import org.vmstudio.visor.core.client.utils.LangHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.vmstudio.visor.core.client.ClientContext;

public class VRClientSettingsManager {
    public static VRClientSettingsManager instance;

    private final Map<String, VROptionRecord> allOptions = new HashMap<>();
    private final Map<String, VROptionWidgetType> optionWidgets = new HashMap<>();

    @Getter
    private final ConfigFile config;
    private Config defaultSettings;


    @Getter
    private final OverlayConfigsManager overlayConfigsAccessor;


    @Getter
    private final VRSettingsPresetRegistry presetsRegistry;

    @Getter
    private ConfigCatalog presetsCatalog;


    public VRClientSettingsManager() {
        instance = this;


        VRPresetSettingsTypeImpl.init();

        ConfigManager configManager = ClientContext.visor.getConfigManager();
        try {
            config = configManager.createConfigFile(
                    ConfigType.YAML,
                    "settings",
                    Path.of("settings.yml"),
                    false
            );
        }catch (Exception e){
            throw new VRException(e);
        }

        initOptionFields();

        updateDefaultOptions();

        loadOptions();

        //to sync config with fields
        saveOptions();

        overlayConfigsAccessor = new OverlayConfigsManager();

        presetsRegistry = new VRSettingsPresetRegistry();

        presetsCatalog = ClientContext.visor
                .getConfigManager()
                .createCatalog(
                        ConfigType.YAML,
                        "custom_presets",
                        Path.of("custom_presets"),
                        true,
                        new PresetsCatalogListener()
                );
    }

    public void saveOptions() {
        applyOptionsTo(config);
        try {
            config.save();
        } catch (Exception exception) {
            VisorClientImpl.LOGGER.info(
                    "------Failed to save settings data!------"
            );
            LoggerUtils.printError(exception);
        }
    }

    public void loadOptions() {
        loadOptionsFrom(config);
    }
    public void loadDefaults() {
        loadOptionsFrom(defaultSettings, true);
    }


    public void updateDefaultOptions() {
        defaultSettings = ClientContext.visor.getConfigManager()
                .createConfig(ConfigType.YAML,null);

        applyOptionsTo(defaultSettings);
    }

    public Config createPresetSnapshot() {
        Config config = ClientContext.visor.getConfigManager()
                .createConfig(ConfigType.YAML, null);
        applyOptionsTo(config, true);
        return config;
    }

    public void applyOptionsTo(@NotNull Config config) {
        applyOptionsTo(config, false);
    }

    public void applyOptionsTo(@NotNull Config config, boolean forPreset) {
        try {
            for (Map.Entry<String, VROptionRecord> entry : allOptions.entrySet()) {
                String optionKey = entry.getKey();
                VROptionRecord optionRecord = entry.getValue();

                if (forPreset && optionRecord.excludeForcedChange()) {
                    continue;
                }
                Field field = optionRecord.field();
                Class<?> fieldType = field.getType();
                Object fieldValue = field.get(null);

                try {
                    Object value = prepareValueForSave(
                            fieldValue,
                            fieldType
                    );
                    config.set(optionKey, value);

                } catch (Exception e) {
                    VisorClientImpl.LOGGER.warn("Failed to save VR option: " + optionKey);
                    LoggerUtils.printError(e);
                }
            }
        } catch (Exception ex) {
            VisorClientImpl.LOGGER.warn("Failed to save VR options: ");
            LoggerUtils.printError(ex);
        }
    }

    public void loadOptionsFrom(@NotNull Config config) {
        loadOptionsFrom(config, false);
    }

    public void loadOptionsFrom(@NotNull Config config, boolean exclude) {
        try {
            for(Map.Entry<String, VROptionRecord> entry
                    : allOptions.entrySet()){
                try {
                    if(exclude && entry.getValue().excludeForcedChange()){
                        continue;
                    }
                    Object value = config.get(entry.getKey());
                    if(value == null) continue;

                    Field field = entry.getValue().field();
                    Class<?> fieldType = field.getType();

                    Object result = Objects.requireNonNull(
                            prepareValueForLoad(
                                    value,
                                    fieldType
                            )
                    );
                    field.set(null, result);

                } catch (Exception exception) {
                    VisorClientImpl.LOGGER.info("Failed to load VR option: " + entry.getKey());
                    LoggerUtils.printError(exception);
                }
            }
        } catch (Exception ex) {
            VisorClientImpl.LOGGER.info("Failed to load VR options!");
            LoggerUtils.printError(ex);
        }
    }


    public void setOptionValue(@NotNull String key,
                               @NotNull Object value) {
        try {
            VROptionRecord optionRecord = allOptions.get(key);
            if (optionRecord == null) {
                return;
            }
            var optionWidget = optionWidgets.get(key);
            optionRecord.field().set(null, value);
            if(optionWidget != null) {
                optionWidget.getBehaviour().onChanged();
            }
            this.saveOptions();
        } catch (Exception exception) {
            System.out.println("Failed to set VR option field: " + key);
            LoggerUtils.printError(exception);
        }
    }

    public Object getOptionValue(@NotNull String key) {
        try {
            VROptionRecord optionRecord = allOptions.get(key);
            if (optionRecord == null) {
                return null;
            }
            return optionRecord.field().get(null);

        } catch (Exception exception) {
            System.out.println("Failed to get VR option field: " + key);
            LoggerUtils.printError(exception);
        }
        return null;
    }

    public void nextOptionValue(@NotNull String key) {
        try {
            VROptionRecord optionRecord = allOptions.get(key);
            if (optionRecord == null) {
                return;
            }
            Field field = optionRecord.field();
            Class<?> fieldType = field.getType();

            var optionWidget = optionWidgets.get(key);
            Object newValue = optionWidget != null ?
                    optionWidget.getBehaviour().nextValue(field.get(null))
                    : null;
            if (newValue == null) {
                if (fieldType == Boolean.TYPE) {
                    newValue = !(boolean) field.get(null);
                } else if (fieldType.isEnum()) {
                    Object[] enumConstants = ((Class<? extends Enum<?>>) fieldType)
                            .getEnumConstants();
                    int currentIndex = ((Enum<?>) field.get(null)).ordinal();
                    newValue = enumConstants[
                            (currentIndex + 1) % enumConstants.length
                            ];
                } else {
                    VisorClientImpl.LOGGER.info("Failed to find next VR option value"
                            + optionRecord.key() + " with type "
                            + fieldType.getSimpleName()
                    );
                    return;
                }
            }
            setOptionValue(key, newValue);
        } catch (Exception exception) {
            System.out.println("Failed to find next VR option value: " + key);
            LoggerUtils.printError(exception);
        }
    }

    public void loadDefaultOptionValue(@NotNull String key) {
        try {
            VROptionRecord optionRecord = allOptions.get(key);
            if (optionRecord == null) {
                return;
            }
            String optionKey = optionRecord.key();
            Object value = defaultSettings.get(optionKey);
            if(value == null) return;

            Field field = optionRecord.field();
            Class<?> fieldType = field.getType();

            Object result = Objects.requireNonNull(
                    prepareValueForLoad(
                            value,
                            fieldType
                    )
            );
            field.set(null, result);

        } catch (Exception ex) {
            VisorClientImpl.LOGGER.info("Failed to load default VR option: " + key);
            LoggerUtils.printError(ex);
        }
    }


    public String getOptionButtonName(VROptionWidgetType optionWidget) {
        return getOptionButtonName(optionWidget, false);
    }

    public String getOptionButtonName(VROptionWidgetType optionWidget,
                                      boolean valueOnly) {
        String lang = LangHelper.getText(
                "visor.options." + optionWidget.getKey()
        );
        String text = lang + ": ";
        if (valueOnly) {
            text = "";
        }

        try {
            VROptionRecord optionRecord = allOptions.get(optionWidget.getKey());
            if (optionRecord == null) {
                return lang;
            }
            Field field = optionRecord.field();
            Class<?> fieldType = field.getType();

            Object currentValue = field.get(null);

            String optionString = optionWidget.getBehaviour().getDisplayString(text, currentValue);
            if (optionString != null) {
                return optionString;
            }

            if (fieldType == Boolean.TYPE) {
                return (boolean) currentValue
                        ? text + LangHelper.getOn()
                        : text + LangHelper.getOff();
            }
            if (fieldType == Float.TYPE || fieldType == Double.TYPE) {
                return text + String.format(
                        "%.2f", ((Number) currentValue).floatValue()
                );
            }
            if (currentValue instanceof Enum<?> enumValue) {
                return text + LangHelper.getText(
                        getEnumOptionLangKey(enumValue)
                );
            }
            return text + currentValue.toString();
        } catch (Exception exception) {
            System.out.println("Failed to get VR option display " +
                    "string for button: " + optionWidget);
            LoggerUtils.printError(exception);
        }

        return "ERROR OCCURRED";
    }

    private String getEnumOptionLangKey(Enum<?> type) {
        switch (type.name().toLowerCase()) {
            case "on":
                return LangHelper.ON_KEY;
            case "off":
                return LangHelper.OFF_KEY;
        }

        Class<?> clazz = type.getClass();

        String enumId = (!clazz.isAnonymousClass()
                ? clazz
                : clazz.getSuperclass()
        ).getSimpleName();

        String enumName = type.name();

        return "visor.options.enums." + enumId + "." + enumName;
    }



    private void initOptionFields() {
        try {
            for (Field field : VRClientSettings.class.getDeclaredFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())){
                    continue;
                }
                field.setAccessible(true);
                VROptionField annotation = field.getAnnotation(VROptionField.class);
                if (annotation == null) {
                    continue;
                }

                String optionKey = annotation.key().isEmpty()
                        ? field.getName() : annotation.key();
                var category = annotation.category();
                if(category == VROptionCategory.EMPTY){
                    category = annotation.widgetType()
                            .getCategory();
                }
                if(category != VROptionCategory.EMPTY){
                    optionKey = category.getKey()
                            + "."
                            + optionKey;
                }


                var optionRecord = new VROptionRecord(
                        field,
                        annotation.widgetType(),
                        optionKey,
                        annotation.excludeForcedChange()
                );

                if (annotation.widgetType() != VROptionWidgetType.EMPTY) {
                    if (optionWidgets.containsValue(annotation.widgetType())) {
                        throw new RuntimeException(
                                "duplicate option widget in client settings! " +
                                        "field: " + annotation.widgetType()
                        );
                    }
                    annotation.widgetType().setKey(optionKey);
                    optionWidgets.put(optionKey, annotation.widgetType());
                }

                allOptions.put(optionKey, optionRecord);
            }
        } catch (Exception ex) {
            throw new VRException(ex);
        }
    }

    private Object prepareValueForSave(Object fieldValue,
                                       Class<?> fieldType){
        if(fieldType.isEnum()){
            return fieldValue.toString();
        }
        if(fieldType.isAssignableFrom(Quaternionf.class)
                && fieldValue instanceof Quaternionf value){
            return String.format("%s;%s;%s;%s", value.x, value.y, value.z, value.w);
        }
        if(fieldType.isAssignableFrom(AtumColor.class)
                && fieldValue instanceof AtumColor value){
            return value.getRedInt()+";"+value.getGreenInt()+";"+value.getBlueInt()+";"+value.getAlphaInt();
        }
        return fieldValue;
    }

    private Object prepareValueForLoad(Object configValue,
                                       Class<?> fieldType){
        if(fieldType.isEnum()){
            Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;
            try {
                return Enum.valueOf(enumType, configValue.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return enumType.getEnumConstants()[0];
            }
        }
        if(fieldType.isAssignableFrom(Quaternionf.class)){
            String[] split = configValue.toString().split(";");
            float w = Float.parseFloat(split[3]);
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            float z = Float.parseFloat(split[2]);
            return new Quaternionf(x, y, z, w);
        }
        if(fieldType.isAssignableFrom(AtumColor.class)){
            String[] split = configValue.toString().split(";");
            int red = Integer.parseInt(split[0]);
            int green = Integer.parseInt(split[1]);
            int blue = Integer.parseInt(split[2]);
            int alpha = Integer.parseInt(split[3]);
            if(fieldType.isAssignableFrom(AtumColorMutable.class)){
                return AtumColor.mutable(red, green, blue, alpha);
            }else {
                return AtumColor.immutable(red, green, blue, alpha);
            }
        }
        if(fieldType.isAssignableFrom(Byte.class)
                || fieldType.isAssignableFrom(byte.class)){
            return ((Number)configValue).byteValue();
        }
        if(fieldType.isAssignableFrom(Short.class)
                || fieldType.isAssignableFrom(short.class)){
            return ((Number)configValue).shortValue();
        }
        if(fieldType.isAssignableFrom(Integer.class)
                || fieldType.isAssignableFrom(int.class)){
            return ((Number)configValue).intValue();
        }
        if(fieldType.isAssignableFrom(Long.class)
                || fieldType.isAssignableFrom(long.class)){
            return ((Number)configValue).longValue();
        }
        if(fieldType.isAssignableFrom(Float.class)
                || fieldType.isAssignableFrom(float.class)){
            return ((Number)configValue).floatValue();
        }
        if(fieldType.isAssignableFrom(Double.class)
                || fieldType.isAssignableFrom(double.class)){
            return ((Number)configValue).doubleValue();
        }
        return configValue;

    }

}


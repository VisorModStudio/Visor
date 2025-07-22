/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package me.phoenixra.visor.core.client.settings;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumconfig.api.placeholders.PlaceholderHandler;
import me.phoenixra.atumconfig.api.placeholders.types.StaticPlaceholder;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.settings.lang.LangHandler;
import me.phoenixra.visor.core.client.settings.option.VROptionField;
import me.phoenixra.visor.core.client.settings.option.VROptionRecord;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import me.phoenixra.visor.core.client.settings.overlays.OverlayCatalogsManager;
import org.joml.Quaternionf;

import java.awt.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.phoenixra.visor.core.client.ClientContext;

public class VRClientSettingsHandler {
    public static VRClientSettingsHandler instance;

    private final Map<VRGuiOption, VROptionRecord> guiOptions = new EnumMap<>(VRGuiOption.class);
    private final Map<String, VROptionRecord> allOptions = new HashMap<>();

    private Config defaultSettings;
    private final ConfigFile settings;

    @Getter
    private final OverlayCatalogsManager overlaysAccessor;

    private boolean wasInit;

    public VRClientSettingsHandler() throws Throwable{
        instance = this;

        ConfigManager configManager = ClientContext.visor.getConfigManager();
        settings = ClientContext.visor.getConfigManager().createConfigFile(
                ConfigType.YAML,
                "settings",
                Path.of("settings.yml"),
                false
        );


        initOptionFields();

        saveDefaultOptions();

        loadOptions();

        //to sync config with fields
        saveOptions();

        overlaysAccessor = new OverlayCatalogsManager();

        PlaceholderHandler placeholderHandler = configManager.getPlaceholderHandler().get();

        if(!wasInit) {
            placeholderHandler.registerGlobalPlaceholder(
                    new StaticPlaceholder(
                            "left_handed",
                            () -> String.valueOf(VRClientSettings.isLeftHanded() ? 1 : 0)
                    )
            );
            placeholderHandler.registerGlobalPlaceholder(
                    new StaticPlaceholder(
                            "right_handed",
                            () -> String.valueOf(!VRClientSettings.isLeftHanded() ? 1 : 0)
                    )
            );
            placeholderHandler.registerGlobalPlaceholder(
                    new StaticPlaceholder(
                            "main_hand",
                            () -> String.valueOf(VRClientSettings.isLeftHanded() ? -1 : 1)
                    )
            );
            placeholderHandler.registerGlobalPlaceholder(
                    new StaticPlaceholder(
                            "tick",
                            () -> String.valueOf(VisorState.TICK_COUNT)
                    )
            );
            placeholderHandler.registerGlobalPlaceholder(
                    new StaticPlaceholder(
                            "frame",
                            () -> String.valueOf(VisorState.FRAME_COUNT)
                    )
            );
        }

        wasInit = true;
    }


    public static void init() {
        try {
            ClientContext.settingsHandler = new VRClientSettingsHandler();
        }catch (Throwable e){
            LoggerUtils.printError(e);
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
        if(fieldType.isAssignableFrom(Color.class)
                && fieldValue instanceof Color value){
            return value.getRed()+";"+value.getGreen()+";"+value.getBlue()+";"+value.getAlpha();
        }
        return fieldValue;
    }

    private Object prepareValueForLoad(Object configValue,
                                       Class<?> fieldType){
        if(fieldType.isEnum()){
            Class<? extends Enum> enumType = (Class<? extends Enum>)
                    fieldType;
            return Enum.valueOf(enumType, configValue.toString().toUpperCase());
        }
        if(fieldType.isAssignableFrom(Quaternionf.class)){
            String[] split = configValue.toString().split(";");
            float w = Float.parseFloat(split[3]);
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            float z = Float.parseFloat(split[2]);
            return new Quaternionf(x, y, z, w);
        }
        if(fieldType.isAssignableFrom(Color.class)){
            String[] split = configValue.toString().split(";");
            int red = Integer.parseInt(split[0]);
            int green = Integer.parseInt(split[1]);
            int blue = Integer.parseInt(split[2]);
            int alpha = Integer.parseInt(split[3]);
            return new Color(red, green, blue, alpha);
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

    public void saveOptions() {
        saveOptions(settings);
        try {
            settings.save();
        } catch (Exception exception) {
            VisorClientImpl.LOGGER.info(
                    "------Failed to save settings data!------"
            );
            LoggerUtils.printError(exception);
        }
    }

    private void saveDefaultOptions() {
        defaultSettings = ClientContext.visor.getConfigManager()
                .createConfig(ConfigType.YAML,null);

        saveOptions(defaultSettings);
    }

    private void saveOptions(Config config) {
        try {
            for (Map.Entry<String, VROptionRecord> entry : allOptions.entrySet()) {
                String optionKey = entry.getKey();
                VROptionRecord optionRecord = entry.getValue();

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

    public void loadOptions() {
        loadOptions(settings);
    }
    private void loadOptions(Config config) {
        try {
            for(Map.Entry<String, VROptionRecord> entry
                    : allOptions.entrySet()){
                try {
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


    public void loadDefaultGuiOption(VRGuiOption option) {
        try {
            VROptionRecord optionRecord = guiOptions.get(option);
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
            VisorClientImpl.LOGGER.info("Failed to load default VR option: " + option);
            LoggerUtils.printError(ex);
        }
    }

    public void updateGuiOptionValue(VRGuiOption optionType) {
        try {
            VROptionRecord optionRecord = guiOptions.get(optionType);
            if (optionRecord == null) {
                return;
            }
            Field field = optionRecord.field();
            Class<?> fieldType = field.getType();

            Object newValue = optionType.updateValue(field.get(null));
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
                    VisorClientImpl.LOGGER.info("Failed to set VR option "
                            + optionRecord.key() + " with type "
                            + fieldType.getSimpleName()
                    );
                    return;
                }
            }

            field.set(null, newValue);
            optionType.onChanged();
            this.saveOptions();
        } catch (Exception exception) {
            System.out.println("Failed to set VR option: " + optionType);
            LoggerUtils.printError(exception);
        }
    }
    public void setGuiOptionValue(VRGuiOption optionType,
                                  Object value) {
        try {
            VROptionRecord optionRecord = guiOptions.get(optionType);
            if (optionRecord == null) {
                return;
            }

            Object newValue = Objects.requireNonNullElse(
                    optionType.setValue(value),
                    value
            );
            optionRecord.field().set(null, newValue);

            optionType.onChanged();
            this.saveOptions();
        } catch (Exception exception) {
            System.out.println("Failed to set VR option: " + optionType);
            LoggerUtils.printError(exception);
        }
    }

    public float getGuiOptionSliderValue(VRGuiOption option) {
        try {
            VROptionRecord optionRecord = guiOptions.get(option);
            if (optionRecord == null) {
                return 0;
            }
            Field field = optionRecord.field();

            float value = ((Number) field.get(null)).floatValue();
            return Objects.requireNonNullElse(
                    option.getSliderValue(value),
                    value
            );
        } catch (Exception exception) {
            System.out.println("Failed to get VR option float value: " + option);
            LoggerUtils.printError(exception);
        }

        return 0.0f;
    }

    public String getButtonDisplayString(VRGuiOption guiOptionType) {
        return getButtonDisplayString(guiOptionType, false);
    }
    public String getButtonDisplayString(VRGuiOption guiOptionType,
                                         boolean valueOnly) {
        String lang = LangHandler.getText(
                "visor.option." + guiOptionType.name()
        );
        String text = lang + ": ";
        if (valueOnly) {
            text = "";
        }

        try {
            VROptionRecord optionRecord = guiOptions.get(guiOptionType);
            if (optionRecord == null) {
                return lang;
            }
            Field field = optionRecord.field();
            Class<?> fieldType = field.getType();

            Object currentValue = field.get(null);

            String optionString = guiOptionType.getDisplayString(text, currentValue);
            if (optionString != null) {
                return optionString;
            }

            if (fieldType == Boolean.TYPE) {
                return (boolean) currentValue
                        ? text + LangHandler.getOn()
                        : text + LangHandler.getOff();
            }
            if (fieldType == Float.TYPE || fieldType == Double.TYPE) {
                if (guiOptionType.isShowAsPercentage()) {
                    return text + Math.round(
                            ((Number) currentValue).floatValue() * 100
                    ) + "%";
                }
                return text + String.format(
                        "%.2f", ((Number) currentValue).floatValue()
                );
            }
            if (currentValue instanceof Enum<?> enumValue) {
                return text + LangHandler.getText(
                        getEnumOptionLangKey(enumValue)
                );
            }
            return text + currentValue.toString();
        } catch (Exception exception) {
            System.out.println("Failed to get VR option display " +
                    "string for button: " + guiOptionType);
            LoggerUtils.printError(exception);
        }

        return "ERROR OCCURRED";
    }
    private String getEnumOptionLangKey(Enum<?> type) {
        switch (type.name().toLowerCase()) {
            case "on":
                return LangHandler.ON_KEY;
            case "off":
                return LangHandler.OFF_KEY;
        }

        Class<?> clazz = type.getClass();

        String enumId = (!clazz.isAnonymousClass()
                ? clazz
                : clazz.getSuperclass()
        ).getSimpleName();

        String enumName = type.name();

        return "visor.enums." + enumId + "." + enumName;
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
                VROptionRecord optionRecord = new VROptionRecord(
                        field,
                        annotation.guiOptionType(),
                        optionKey
                );

                //GUI OPTIONS
                if (annotation.guiOptionType() != VRGuiOption.NONE) {
                    if (guiOptions.containsKey(annotation.guiOptionType())) {
                        throw new RuntimeException(
                                "duplicate gui option in client settings " +
                                        "field: " + annotation.guiOptionType()
                        );
                    }
                    guiOptions.put(annotation.guiOptionType(), optionRecord);
                }

                //ALL
                allOptions.put(optionKey, optionRecord);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}


package org.vmstudio.visor.api.client.gui.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;


@Getter
public enum VRPresetSettingsType {
    GENERAL("general"),
    ACTION_BINDINGS("action_bindings"),
    OVERLAYS("overlays");

    private final String key;
    private final Component name;


    @Setter
    private Supplier<Config> serializer;

    @Setter
    private Consumer<Config> loader;


    VRPresetSettingsType(@NotNull String key){
        this.key = key;
        this.name = Component.translatable("visor.options.presets.settings_type."+key);
    }

    public static VRPresetSettingsType fromId(@NotNull String id){
        if(VRPresetSettingsType.ACTION_BINDINGS.key.equals(id)){
            return ACTION_BINDINGS;
        }
        if(VRPresetSettingsType.OVERLAYS.key.equals(id)){
            return OVERLAYS;
        }
        return GENERAL;
    }

}

package me.phoenixra.visor.core.client.settings;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public enum VROptionCategory {
    EMPTY("empty"),
    MAIN("main"),
    GUI("gui"),
    RENDERING("rendering"),
    RENDERING_EYE_EFFECTS("rendering.eye_effects"),
    RENDERING_THIRD_PERSON("rendering.third_person"),
    RENDERING_MIXED_REALITY("rendering.mixed_reality"),
    MOVEMENT("movement"),
    CONTROLS("controls"),
    IMMERSION("immersion");

    @Getter
    private final String key;

    VROptionCategory(@NotNull String key){
        this.key = key;
    }

    public List<VROptionWidgetType> types() {
        return Arrays.stream(VROptionWidgetType.values())
                .filter(t -> t.getCategory() == this)
                .toList();
    }
}

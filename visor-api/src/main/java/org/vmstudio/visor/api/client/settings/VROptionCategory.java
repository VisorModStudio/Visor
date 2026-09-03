package org.vmstudio.visor.api.client.settings;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum VROptionCategory {
    EMPTY("empty"),
    MAIN("main"),
    GUI("gui"),
    GUI_HOTBAR("gui.hotbar"),
    RENDERING("rendering"),
    RENDERING_SHADERS("rendering.shaders"),
    RENDERING_EFFECTS("rendering.effects"),
    RENDERING_EFFECTS_EYE_EFFECTS("rendering.effects.eye_effects"),
    RENDERING_THIRD_PERSON("rendering.third_person"),
    RENDERING_MIXED_REALITY("rendering.mixed_reality"),
    VR_BODY("vr_body"),
    VR_BODY_HEIGHT("vr_body.height"),
    MOVEMENT("movement"),
    CONTROLS("controls"),
    IMMERSION("immersion"),
    IMMERSION_ADVANCED("immersion.advanced"),
    WORLD("world");

    @Getter
    private final String key;

    VROptionCategory(@NotNull String key){
        this.key = key;
    }
}

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
    RENDERING_EYE_EFFECTS("rendering.eye_effects"),
    RENDERING_THIRD_PERSON("rendering.third_person"),
    RENDERING_MIXED_REALITY("rendering.mixed_reality"),
    VR_BODY("vr_body"),
    MOVEMENT("movement"),
    CONTROLS("controls"),
    IMMERSION("immersion"),
    IMMERSION_ADVANCED("immersion.advanced");

    @Getter
    private final String key;

    VROptionCategory(@NotNull String key){
        this.key = key;
    }
}

package org.vmstudio.visor.api.client.settings.enums;

import me.phoenixra.atumvr.core.enums.XRGraphicsApi;
import org.jetbrains.annotations.Nullable;

/**
 * Graphics API used to submit frames to the OpenXR runtime.
 * <p>
 *     AUTO lets AtumVR pick: OpenGL when the runtime supports it,
 *     otherwise Vulkan (VDXR, Intel Arc setups). Rendering always
 *     stays OpenGL. Applied on VR initialization only.
 * </p>
 */
public enum GraphicsApi {
    AUTO,
    OPENGL,
    VULKAN;

    public @Nullable XRGraphicsApi toPreference() {
        return switch (this) {
            case AUTO -> null;
            case OPENGL -> XRGraphicsApi.OPENGL;
            case VULKAN -> XRGraphicsApi.VULKAN;
        };
    }
}

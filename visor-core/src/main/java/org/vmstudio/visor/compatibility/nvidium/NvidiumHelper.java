package org.vmstudio.visor.compatibility.nvidium;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

import java.lang.reflect.Field;

public class NvidiumHelper {
    private NvidiumHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }
    private static final String MOD_ID = "nvidium";
    private static boolean initialized;
    private static Field Nvidium_IS_ENABLED;
    private static Field Nvidium_config;
    private static Field NvidiumConfig_enable_temporal_coherence;
    private static boolean loggedTemporalForce;
    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean isRendererActive() {
        if (!init()) {
            return false;
        }
        try {
            return Nvidium_IS_ENABLED.getBoolean(null);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static boolean shouldKeepCommandBuffer() {
        if (VisorState.get().isNotActive() || !isRendererActive()) {
            return false;
        }
        if (VRRenderState.getPhase().isVanilla()) {
            return false;
        }
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        return renderPass.isWorld() && !renderPass.isEye();
    }

    public static void ensureVRTemporalCoherence() {
        if (!isRendererActive()) {
            return;
        }
        try {
            Object config = Nvidium_config.get(null);
            if (config == null || NvidiumConfig_enable_temporal_coherence.getBoolean(config)) {
                return;
            }
            NvidiumConfig_enable_temporal_coherence.setBoolean(config, true);
            if (!loggedTemporalForce) {
                loggedTemporalForce = true;
                VisorClientImpl.LOGGER.info(
                        "Visor: forced nvidium temporal coherence on for VR (session only)"
                );
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    private static boolean init() {
        if (initialized) {
            return Nvidium_IS_ENABLED != null;
        }
        initialized = true;
        if (!isLoaded()) {
            return false;
        }
        try {
            Class<?> nvidium = Class.forName("me.cortex.nvidium.Nvidium");
            Class<?> nvidiumConfig = Class.forName("me.cortex.nvidium.config.NvidiumConfig");
            Nvidium_IS_ENABLED = nvidium.getField("IS_ENABLED");
            Nvidium_config = nvidium.getField("config");
            NvidiumConfig_enable_temporal_coherence =
                    nvidiumConfig.getField("enable_temporal_coherence");
            return true;
        } catch (Throwable t) {
            Nvidium_IS_ENABLED = null;
            VisorClientImpl.LOGGER.warn(
                    "Visor: nvidium is present but has an unexpected error, "
                            + "VR compatibility are disabled", t
            );
            return false;
        }
    }
}
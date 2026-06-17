package org.vmstudio.visor.compatibility.iris;

import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;

import java.lang.reflect.Method;

public final class IrisDhOverrideHelper {
    private static boolean initialized;
    private static boolean ready;

    private static Object overrideInjector;
    private static Method overrideInjectorUnbind;
    private static Class<?> framebufferInterface;
    private static Class<?> genericShaderInterface;

    private static Method pipelineGetDhCompat;
    private static Method dhCompatGetInstance;
    private static Method getShadowFBWrapper;
    private static Method getSolidFBWrapper;
    private static Method getGenericShader;

    private IrisDhOverrideHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void unregisterDhOverrides(Object pipeline) {
        if (pipeline == null || !init()) {
            return;
        }
        try {
            Object dhCompat = pipelineGetDhCompat.invoke(pipeline);
            if (dhCompat == null) {
                return;
            }
            Object instance = dhCompatGetInstance.invoke(dhCompat);
            if (instance == null) {
                return;
            }
            unbind(framebufferInterface, getShadowFBWrapper.invoke(instance));
            unbind(framebufferInterface, getSolidFBWrapper.invoke(instance));
            if (getGenericShader != null) {
                unbind(genericShaderInterface, getGenericShader.invoke(instance));
            }
        } catch (Throwable t) {
            ready = false;
            LoggerUtils.getLogger().error("Visor: failed to unbind the DH overrides on pipeline swap", t);
        }
    }

    private static void unbind(Class<?> overrideInterface, Object override) throws Exception {
        if (override != null) {
            overrideInjectorUnbind.invoke(overrideInjector, overrideInterface, override);
        }
    }

    private static boolean init() {
        if (initialized) {
            return ready;
        }
        initialized = true;
        if (!DhCompatHelper.isLoaded() || !IrisCompatHelper.isLoaded()) {
            return false;
        }
        try {
            Class<?> injectorClass = Class.forName(
                    "com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector");
            overrideInjector = injectorClass.getDeclaredField("INSTANCE").get(null);
            overrideInjectorUnbind = injectorClass.getMethod("unbind", Class.class, Class.forName(
                    "com.seibel.distanthorizons.api.interfaces.override.IDhApiOverrideable"));
            framebufferInterface = Class.forName(
                    "com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer");

            pipelineGetDhCompat = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline")
                    .getMethod("getDHCompat");
            dhCompatGetInstance = Class.forName("net.irisshaders.iris.compat.dh.DHCompat")
                    .getMethod("getInstance");
            Class<?> compatInternal = Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal");
            getShadowFBWrapper = compatInternal.getMethod("getShadowFBWrapper");
            getSolidFBWrapper = compatInternal.getMethod("getSolidFBWrapper");

            try {
                genericShaderInterface = Class.forName(
                        "com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiGenericObjectShaderProgram");
                getGenericShader = compatInternal.getMethod("getGenericShader");
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                genericShaderInterface = null;
                getGenericShader = null;
            }

            ready = true;
        } catch (Throwable t) {
            LoggerUtils.getLogger().error(
                    "Visor: DH present but the Iris DH override unbinding could not be mapped; "
                            + "DH LODs will be wrong with shaders in VR", t);
        }
        return ready;
    }
}
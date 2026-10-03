package org.vmstudio.visor.compatibility.voxy;

import net.irisshaders.iris.Iris;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;
import org.vmstudio.visor.api.common.utils.LoggerUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VoxyIrisPipelineBinder {
    private static boolean initialized;
    private static boolean ready;

    private static Class<?> dataHolderInterface;
    private static Method getPipelineData;

    private static Field pipelineData;
    private static Field pipelineFb;
    private static Field pipelineFbTranslucent;
    private static Field dataThePipeline;
    private static Field dataOpaqueDrawTargets;
    private static Field dataTranslucentDrawTargets;
    private static Field depthFramebufferFramebuffer;
    private static Field glFramebufferId;

    public static void rebindVoxyToIrisPipeline(Object voxyPipeline) {
        if (initialized && !ready) {
            return;
        }
        try {
            rebind(voxyPipeline, Iris.getPipelineManager().getPipelineNullable());
        } catch (Throwable t) {
            ready = false;
            LoggerUtils.getLogger().error(
                    "Visor: failed to rebind Voxy to the current Iris pipeline; "
                            + "Voxy LODs will be wrong with shaders in VR", t);
        }
    }

    private static void rebind(Object voxyPipeline, Object irisPipeline) throws Exception {
        if (!init(voxyPipeline.getClass())) {
            return;
        }
        if (!dataHolderInterface.isInstance(irisPipeline)) {
            return;
        }
        Object current = getPipelineData.invoke(irisPipeline);
        Object bound = pipelineData.get(voxyPipeline);
        if (current == null || current == bound) {
            return;
        }

        dataThePipeline.set(bound, null);
        dataThePipeline.set(current, voxyPipeline);
        pipelineData.set(voxyPipeline, current);

        attachDrawTargets(framebufferId(pipelineFb.get(voxyPipeline)),
                (int[]) dataOpaqueDrawTargets.get(current), ((int[]) dataOpaqueDrawTargets.get(bound)).length);
        attachDrawTargets(framebufferId(pipelineFbTranslucent.get(voxyPipeline)),
                (int[]) dataTranslucentDrawTargets.get(current), ((int[]) dataTranslucentDrawTargets.get(bound)).length);
    }

    private static int framebufferId(Object depthFramebuffer) throws Exception {
        return glFramebufferId.getInt(depthFramebufferFramebuffer.get(depthFramebuffer));
    }

    private static void attachDrawTargets(int framebuffer, int[] targets, int previousCount) {
        int[] drawBuffers = new int[targets.length];
        for (int i = 0; i < targets.length; i++) {
            drawBuffers[i] = GL30C.GL_COLOR_ATTACHMENT0 + i;
            GL45C.glNamedFramebufferTexture(framebuffer, GL30C.GL_COLOR_ATTACHMENT0 + i, targets[i], 0);
        }
        for (int i = targets.length; i < previousCount; i++) {
            GL45C.glNamedFramebufferTexture(framebuffer, GL30C.GL_COLOR_ATTACHMENT0 + i, 0, 0);
        }
        GL45C.glNamedFramebufferDrawBuffers(framebuffer, drawBuffers);
    }

    private static boolean init(Class<?> pipelineClass) throws Exception {
        if (initialized) {
            return ready;
        }
        initialized = true;

        dataHolderInterface = Class.forName("me.cortex.voxy.client.iris.IGetIrisVoxyPipelineData",
                false, pipelineClass.getClassLoader());
        getPipelineData = dataHolderInterface.getMethod("voxy$getPipelineData");
        Class<?> dataClass = getPipelineData.getReturnType();

        pipelineData = pipelineClass.getDeclaredField("data");
        pipelineData.setAccessible(true);
        pipelineFb = pipelineClass.getField("fb");
        pipelineFb.setAccessible(true);
        pipelineFbTranslucent = pipelineClass.getField("fbTranslucent");
        pipelineFbTranslucent.setAccessible(true);
        dataThePipeline = dataClass.getField("thePipeline");
        dataThePipeline.setAccessible(true);
        dataOpaqueDrawTargets = dataClass.getField("opaqueDrawTargets");
        dataOpaqueDrawTargets.setAccessible(true);
        dataTranslucentDrawTargets = dataClass.getField("translucentDrawTargets");
        dataTranslucentDrawTargets.setAccessible(true);
        depthFramebufferFramebuffer = pipelineFb.getType().getField("framebuffer");
        depthFramebufferFramebuffer.setAccessible(true);
        glFramebufferId = depthFramebufferFramebuffer.getType().getField("id");
        glFramebufferId.setAccessible(true);

        ready = true;
        return true;
    }



    private static Map<Class<?>, Method> NAME_GETTERS = new ConcurrentHashMap<>();
    public static Comparator<Object> UNIFORM_ORDER = Comparator.comparing(VoxyIrisPipelineBinder::uniformName);

    private static boolean loggedNameFailure;

    private static String uniformName(Object uniform) {
        if (uniform == null) {
            return "";
        }
        try {
            Method getter = NAME_GETTERS.computeIfAbsent(uniform.getClass(), VoxyIrisPipelineBinder::findNameGetter);
            return String.valueOf(getter.invoke(uniform));
        } catch (Throwable t) {
            if (!loggedNameFailure) {
                loggedNameFailure = true;
                LoggerUtils.getLogger().error(
                        "Visor: couldnt read the names of Voxy's shader uniforms; "
                                + "Voxy LODs may get garbage uniforms with shaders in VR", t);
            }
            return "";
        }
    }

    private static Method findNameGetter(Class<?> uniformClass) {
        try {
            Method method = uniformClass.getDeclaredMethod("name");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("no name() in " + uniformClass.getName(), e);
        }
    }
}

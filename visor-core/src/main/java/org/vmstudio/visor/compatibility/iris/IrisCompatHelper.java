package org.vmstudio.visor.compatibility.iris;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.atumvr.api.enums.EyeType;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.compatibility.shaders.IrisVRBridge;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class IrisCompatHelper {
    public static final String MOD_ID = "iris";
    public static final String OCULUS_MOD_ID = "oculus";

    public static volatile boolean perEyeFailed = false;

    public static volatile VRRenderPass buildingPass = null;

    public static volatile Object sharedShadowTargets = null;

    public static boolean sharedShadowActive() {
        return sharedShadowTargets != null;
    }

    public static boolean shareShadows() {
        return VRClientSettings.isShaderSharedShadows();
    }
    public static boolean shareSsbo() {
        return VRClientSettings.isShaderSharedSsbo();
    }

    public static void clearIrisFallback() {
        try {
            Class<?> irisClass = findClass("net.irisshaders.iris.Iris", "net.coderbot.iris.Iris");
            java.lang.reflect.Field fallback = irisClass.getDeclaredField("fallback");
            fallback.setAccessible(true);
            fallback.setBoolean(null, false);
        } catch (Throwable t) {
            LoggerUtils.getLogger().warn("Visor: could not reset Iris' fallback flag", t);
        }
    }

    public static Class<?> findClass(String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {}
        }
        throw new ClassNotFoundException(String.join(" / ", names));
    }

    public static void bumpSodiumReloadCounter(Object pipelineManager) {
        try {
            Field counter =
                    pipelineManager.getClass().getDeclaredField("versionCounterForSodiumShaderReload");
            counter.setAccessible(true);
            counter.setInt(pipelineManager, counter.getInt(pipelineManager) + 1);
        } catch (Throwable t) {
            LoggerUtils.getLogger().warn(
                    "Visor: could not bump Iris' Sodium shader reload counter (legacy Iris)", t);
        }
    }

    public static void latchPerEyeOff(Throwable t) {
        if (!perEyeFailed) {
            perEyeFailed = true;
            LoggerUtils.getLogger().error(
                    "Visor: per-pass Iris pipelines failed; falling back to the single shared "
                            + "pipeline for this session.", t);
        }
    }

    public static void resetPackState() {
        slowMode = false;
        sharedShadowTargets = null;
    }

    public static volatile boolean slowMode = false;

    public static boolean perEyePipelines() {
        boolean enabled = VRClientSettings.isShaderPerEyePipelines();
        return enabled && !perEyeFailed;
    }

    private static volatile boolean pipelineReloadRequested = false;

    public static void requestPipelineReload() {
        perEyeFailed = false;
        pipelineReloadRequested = true;
    }

    public static boolean consumePipelineReloadRequest() {
        if (pipelineReloadRequested) {
            pipelineReloadRequested = false;
            return true;
        }
        return false;
    }

    public static boolean syncShadowGrid() {
        if (!perEyePipelines()) {
            return false;
        }
        if (sharedShadowActive()) {
            return true;
        }
        return !slowMode;
    }

    public static boolean syncFrameCounter() {
        return !perEyePipelines();
    }

    private IrisCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID) || ModLoader.get().isModLoaded(OCULUS_MOD_ID);
    }

    public static void prepare(@NotNull VisorAddon owner) {
        if (!isLoaded()) {
            return;
        }
        try {
            ShadersHelper.setBridge(new ReflectiveIrisBridge());
            LoggerUtils.getLogger().info("Visor: Iris VR bridge bound ({}).",
                    ModLoader.get().isModLoaded(OCULUS_MOD_ID) ? "Oculus" : "Iris");
        } catch (Throwable t) {
            LoggerUtils.getLogger().error("Visor: failed to bind Iris VR bridge; shaders will stay inactive.", t);
        }
    }

    private static final class ReflectiveIrisBridge implements IrisVRBridge {
        private boolean initialized;
        private boolean initFailed;

        private Object apiInstance;
        private Method isShaderPackInUse;

        private boolean pipelineControlReady;
        private Method getPipelineManager;
        private Method getPipelineNullable;
        private Method getPipelineOptional;
        private Method destroyPipeline;
        private Method preparePipeline;
        private Method getCurrentDimension;

        private Object knownGoodPipeline;

        private int lastBuiltEyeWidth = -1;
        private int lastBuiltEyeHeight = -1;

        private boolean ensureInitialized() {
            if (initialized) {
                return !initFailed;
            }
            initialized = true;
            try {
                Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                apiInstance = irisApiClass.getMethod("getInstance").invoke(null);
                isShaderPackInUse = irisApiClass.getMethod("isShaderPackInUse");
            } catch (Throwable t) {
                initFailed = true;
                LoggerUtils.getLogger().error(
                        "Visor: failed to map the Iris v0 API (net.irisshaders.iris.api.v0.IrisApi); "
                                + "shaders will be treated as inactive.", t);
                return false;
            }

            try {
                Class<?> irisClass = IrisCompatHelper.findClass(
                        "net.irisshaders.iris.Iris", "net.coderbot.iris.Iris");
                getPipelineManager = irisClass.getMethod("getPipelineManager");
                getCurrentDimension = irisClass.getMethod("getCurrentDimension");
                Class<?> pmClass = getPipelineManager.getReturnType();
                try {
                    getPipelineNullable = pmClass.getMethod("getPipelineNullable");
                } catch (NoSuchMethodException e) {
                    getPipelineNullable = null;
                }
                getPipelineOptional = pmClass.getMethod("getPipeline");
                destroyPipeline = pmClass.getMethod("destroyPipeline");
                for (Method method : pmClass.getMethods()) {
                    if ("preparePipeline".equals(method.getName()) && method.getParameterCount() == 1) {
                        preparePipeline = method;
                        break;
                    }
                }
                if (preparePipeline == null) {
                    throw new NoSuchMethodException("PipelineManager.preparePipeline(<dimension>)");
                }
                pipelineControlReady = true;
            } catch (Throwable t) {
                pipelineControlReady = false;
                LoggerUtils.getLogger().warn(
                        "Visor: Iris pipeline control not available; the eye-target depth "
                                + "rebuild will be skipped (shaders may render incorrectly in VR).", t);
            }

            return true;
        }

        @Override
        public boolean isActive() {
            if (!ensureInitialized()) {
                return false;
            }
            try {
                return (boolean) isShaderPackInUse.invoke(apiInstance);
            } catch (Throwable t) {
                return false;
            }
        }

        @Override
        public boolean sameSizedBuffers() {
            return isActive();
        }

        @Override
        public int getShaderLightValue() {
            return isActive() ? 16 : 8;
        }

        @Override
        public void beginFrame(float partialTicks, long frameNanos) {
            if (IrisCompatHelper.consumePipelineReloadRequest()
                    && ensureInitialized() && pipelineControlReady && isActive()) {
                try {
                    IrisCompatHelper.clearIrisFallback();
                    Object pipelineManager = getPipelineManager.invoke(null);
                    destroyPipeline.invoke(pipelineManager);
                    Object dimension = getCurrentDimension.invoke(null);
                    preparePipeline.invoke(pipelineManager, dimension);
                    knownGoodPipeline = null;
                    LoggerUtils.getLogger().info(
                            "Visor: Iris pipelines rebuilt after a VR shader setting change.");
                } catch (Throwable t) {
                    LoggerUtils.printError(t);
                }
            }

            if (perEyePipelines()) {
                return;
            }
            if (!ensureInitialized() || !pipelineControlReady) {
                return;
            }
            if (!isActive()) {
                diag("no shader pack in use");
                return;
            }
            try {
                Object pipelineManager = getPipelineManager.invoke(null);
                Object pipeline = getPipelineNullable != null
                        ? getPipelineNullable.invoke(pipelineManager)
                        : ((java.util.Optional<?>) getPipelineOptional.invoke(pipelineManager)).orElse(null);

                if (pipeline == null) {
                    diag("shader pack active but Iris has not built a pipeline yet");
                    return;
                }
                if (pipeline == knownGoodPipeline) {
                    diag("pipeline stable (already rebuilt against the eye target)");
                    return;
                }

                RenderTarget eye = VRRenderState.getTargetForPass(VRRenderPass.EYE_LEFT);
                if (eye == null) {
                    diag("eye render target not available yet");
                    return;
                }

                Minecraft mc = Minecraft.getInstance();
                RenderTarget previousMain = mc.mainRenderTarget;
                mc.mainRenderTarget = eye;
                try {
                    destroyPipeline.invoke(pipelineManager);
                    Object dimension = getCurrentDimension.invoke(null);
                    knownGoodPipeline = preparePipeline.invoke(pipelineManager, dimension);
                    lastDiag = null;
                    LoggerUtils.getLogger().info(
                            "Visor: rebuilt the Iris pipeline against the eye target");
                } finally {
                    mc.mainRenderTarget = previousMain;
                }
            } catch (Throwable t) {
                LoggerUtils.printError(t);
            }
        }

        private String lastDiag;

        private void diag(String state) {
            if (!state.equals(lastDiag)) {
                lastDiag = state;
                LoggerUtils.getLogger().info("Visor: Iris VR beginFrame {}", state);
            }
        }

        @Override
        public void beginEye(EyeType eyeType) {
        }

        @Override
        public void endEye() {
        }

        @Override
        public void endFrame() {
        }

        @Override
        public void onVisorTargetsRecreated(int eyeRenderWidth, int eyeRenderHeight) {
            if (eyeRenderWidth != lastBuiltEyeWidth || eyeRenderHeight != lastBuiltEyeHeight) {
                lastBuiltEyeWidth = eyeRenderWidth;
                lastBuiltEyeHeight = eyeRenderHeight;
                knownGoodPipeline = null;
            }
        }

        @Override
        public void onPackChanged() {
            knownGoodPipeline = null;
            IrisCompatHelper.resetPackState();
        }

        @Override
        public void setIsMainBound(boolean bound) {
        }
    }
}

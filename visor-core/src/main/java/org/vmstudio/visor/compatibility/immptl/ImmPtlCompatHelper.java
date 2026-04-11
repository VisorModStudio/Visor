package org.vmstudio.visor.compatibility.immptl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ImmPtlCompatHelper {
    public static final String MOD_ID = "imm_ptl_core";

    private static boolean reflectionInitialized;
    private static boolean reflectionFailed;
    private static boolean reflectionFailureLogged;

    private static @Nullable Field ipRenderModeField;
    private static @Nullable Class<? extends Enum> ipRenderModeClass;
    private static @Nullable Field basicProjectionMatrixField;
    private static @Nullable Method worldRenderInfoIsRenderingMethod;
    private static @Nullable Method portalAwareRayTraceMethod;

    private static @Nullable Object savedRenderMode;
    private static boolean renderModeOverridden;
    private static @Nullable PortalAwareControllerHit controllerHit;

    private ImmPtlCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static void onVrActivated() {
        if (!ensureReflection()) {
            return;
        }

        try {
            if (renderModeOverridden || ipRenderModeField == null || ipRenderModeClass == null) {
                return;
            }

            Object currentMode = ipRenderModeField.get(null);
            if (!(currentMode instanceof Enum<?> currentEnum)) {
                return;
            }
            if (!"normal".equals(currentEnum.name())) {
                return;
            }

            Object compatibilityMode = Enum.valueOf(ipRenderModeClass, "compatibility");
            savedRenderMode = currentMode;
            ipRenderModeField.set(null, compatibilityMode);
            renderModeOverridden = true;
        } catch (Throwable throwable) {
            logReflectionFailure("Failed to switch Immersive Portals to compatibility renderer", throwable);
        }
    }

    public static void onVrDeactivated() {
        restoreRenderMode();
        clearProjectionCache();
        clearControllerHit();
    }

    public static void onBeginVrWorldPass(@Nullable VRRenderPass renderPass) {
        if (renderPass != null && renderPass.isEye()) {
            clearProjectionCache();
        }
    }

    public static void onPortalWorldRenderFinished() {
        clearProjectionCache();
    }

    public static void clearProjectionCache() {
        if (!ensureReflection() || basicProjectionMatrixField == null) {
            return;
        }

        try {
            basicProjectionMatrixField.set(null, null);
        } catch (Throwable throwable) {
            logReflectionFailure("Failed to clear Immersive Portals projection cache", throwable);
        }
    }

    public static boolean isRenderingPortalWorld() {
        if (!ensureReflection() || worldRenderInfoIsRenderingMethod == null) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(worldRenderInfoIsRenderingMethod.invoke(null));
        } catch (Throwable throwable) {
            logReflectionFailure("Failed to query Immersive Portals render state", throwable);
            return false;
        }
    }

    public static HitResult pickBlock(Level level, VRPose vrPose, double reachDistance, boolean fluid, Player player) {
        Vec3 start = new Vec3((Vector3f) vrPose.getPosition());
        Vec3 direction = new Vec3((Vector3f) vrPose.getDirection()).normalize();

        PortalAwareControllerHit portalAwareHit = portalAwareRayTrace(level, start, direction, reachDistance, player);
        if (portalAwareHit != null) {
            controllerHit = portalAwareHit;
            return portalAwareHit.hitResult();
        }

        BlockHitResult fallbackHit = level.clip(
                new ClipContext(
                        start,
                        start.add(direction.scale(reachDistance)),
                        ClipContext.Block.OUTLINE,
                        fluid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                        player
                )
        );
        controllerHit = new PortalAwareControllerHit(level, fallbackHit, List.of());
        return fallbackHit;
    }

    public static @Nullable PortalAwareControllerHit getControllerHit() {
        return controllerHit;
    }

    public static void clearControllerHit() {
        controllerHit = null;
    }

    private static @Nullable PortalAwareControllerHit portalAwareRayTrace(
            Level world,
            Vec3 startingPoint,
            Vec3 direction,
            double maxDistance,
            Entity entity
    ) {
        if (!ensureReflection() || portalAwareRayTraceMethod == null) {
            return null;
        }

        try {
            Object result = portalAwareRayTraceMethod.invoke(
                    null,
                    world,
                    startingPoint,
                    direction,
                    maxDistance,
                    entity
            );
            if (result == null) {
                return null;
            }

            Class<?> resultClass = result.getClass();
            Level hitWorld = (Level) resultClass.getMethod("world").invoke(result);
            BlockHitResult hitResult = (BlockHitResult) resultClass.getMethod("hitResult").invoke(result);
            List<?> rawPortals = (List<?>) resultClass.getMethod("portalsPassingThrough").invoke(result);
            List<Object> portals = rawPortals == null ? List.of() : new ArrayList<>(rawPortals);

            if (hitWorld == null || hitResult == null) {
                return null;
            }
            return new PortalAwareControllerHit(hitWorld, hitResult, portals);
        } catch (Throwable throwable) {
            logReflectionFailure("Failed to perform Immersive Portals controller ray trace", throwable);
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean ensureReflection() {
        if (!isLoaded()) {
            return false;
        }
        if (reflectionInitialized) {
            return !reflectionFailed;
        }

        reflectionInitialized = true;
        try {
            Class<?> ipGlobalClass = Class.forName("qouteall.imm_ptl.core.IPGlobal");
            Class<?> renderStatesClass = Class.forName("qouteall.imm_ptl.core.render.context_management.RenderStates");
            Class<?> worldRenderInfoClass = Class.forName("qouteall.imm_ptl.core.render.context_management.WorldRenderInfo");
            Class<?> portalUtilsClass = Class.forName("qouteall.imm_ptl.core.portal.PortalUtils");

            ipRenderModeField = ipGlobalClass.getField("renderMode");
            ipRenderModeClass = (Class<? extends Enum>) ipRenderModeField.getType().asSubclass(Enum.class);
            basicProjectionMatrixField = renderStatesClass.getField("basicProjectionMatrix");
            worldRenderInfoIsRenderingMethod = worldRenderInfoClass.getMethod("isRendering");
            portalAwareRayTraceMethod = portalUtilsClass.getMethod(
                    "portalAwareRayTrace",
                    Level.class,
                    Vec3.class,
                    Vec3.class,
                    double.class,
                    Entity.class
            );

            reflectionFailed = false;
            return true;
        } catch (Throwable throwable) {
            reflectionFailed = true;
            logReflectionFailure("Failed to initialize Immersive Portals compatibility bridge", throwable);
            return false;
        }
    }

    private static void restoreRenderMode() {
        if (!ensureReflection() || !renderModeOverridden || ipRenderModeField == null || savedRenderMode == null) {
            renderModeOverridden = false;
            savedRenderMode = null;
            return;
        }

        try {
            Object currentMode = ipRenderModeField.get(null);
            if (currentMode instanceof Enum<?> currentEnum && "compatibility".equals(currentEnum.name())) {
                ipRenderModeField.set(null, savedRenderMode);
            }
        } catch (Throwable throwable) {
            logReflectionFailure("Failed to restore Immersive Portals render mode", throwable);
        } finally {
            renderModeOverridden = false;
            savedRenderMode = null;
        }
    }

    private static void logReflectionFailure(String message, Throwable throwable) {
        if (reflectionFailureLogged) {
            return;
        }
        reflectionFailureLogged = true;
        VisorClientImpl.LOGGER.warn(message, throwable);
    }
}
package org.vmstudio.visor.core.client.render.helpers;

import me.phoenixra.atumvr.api.enums.EyeType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.nvidium.NvidiumHelper;
import org.vmstudio.visor.compatibility.sodium.SodiumHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;

public class CullFrustumHelper {
    private static final Matrix4f lastCenterProjection = new Matrix4f();
    private static boolean centerProjectionCaptured;
    private CullFrustumHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static Matrix4f widenCullProjection(Matrix4f projection) {
        if (VisorState.get().isNotActive() || ClientContext.renderer == null) {
            return projection;
        }
        if (!SodiumHelper.isLoaded()) {
            return projection;
        }
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == VRRenderPass.CENTER) {
            lastCenterProjection.set(projection);
            centerProjectionCaptured = true;
            return projection;
        }
        if (!renderPass.isEye()) {
            return projection;
        }
        Matrix4fc left = ClientContext.renderer.getEyeProjection(EyeType.LEFT);
        Matrix4fc right = ClientContext.renderer.getEyeProjection(EyeType.RIGHT);
        if (left == null || right == null) {
            return projection;
        }

        float tanLeft = Math.min(tanLeft(projection), Math.min(tanLeft(left), tanLeft(right)));
        float tanRight = Math.max(tanRight(projection), Math.max(tanRight(left), tanRight(right)));
        float tanDown = Math.min(tanDown(projection), Math.min(tanDown(left), tanDown(right)));
        float tanUp = Math.max(tanUp(projection), Math.max(tanUp(left), tanUp(right)));

        if (centerProjectionCaptured && NvidiumHelper.isRendererActive() && isCenterPassActive()) {
            tanLeft = Math.min(tanLeft, tanLeft(lastCenterProjection));
            tanRight = Math.max(tanRight, tanRight(lastCenterProjection));
            tanDown = Math.min(tanDown, tanDown(lastCenterProjection));
            tanUp = Math.max(tanUp, tanUp(lastCenterProjection));
        }

        double cullFovMargin = Math.toRadians(5);
        tanLeft = expand(tanLeft, -cullFovMargin);
        tanRight = expand(tanRight, cullFovMargin);
        tanDown = expand(tanDown, -cullFovMargin);
        tanUp = expand(tanUp, cullFovMargin);

        if (!(tanRight > tanLeft) || !(tanUp > tanDown)) {
            return projection;
        }

        Matrix4f result = new Matrix4f(projection);
        result.m00(2.0f / (tanRight - tanLeft));
        result.m11(2.0f / (tanUp - tanDown));
        result.m20((tanRight + tanLeft) / (tanRight - tanLeft));
        result.m21((tanUp + tanDown) / (tanUp - tanDown));
        return result;
    }

    private static boolean isCenterPassActive() {
        MirrorMode mode = VRClientSettings.getMirrorMode();
        return mode == MirrorMode.FIRST_PERSON
                || (mode == MirrorMode.MIXED_REALITY
                && VRClientSettings.isMixedRealityWithFirstPerson()
                && VRClientSettings.isMixedRealityAsGrid2x2());
    }

    private static float expand(float tangent, double marginRadians) {
        double angle = Math.atan(tangent) + marginRadians;
        double maxHalfAngle = Math.toRadians(85);
        angle = Math.max(-maxHalfAngle, Math.min(maxHalfAngle, angle));
        return (float) Math.tan(angle);
    }

    private static float tanLeft(Matrix4fc projection) {
        return (projection.m20() - 1.0f) / projection.m00();
    }

    private static float tanRight(Matrix4fc projection) {
        return (projection.m20() + 1.0f) / projection.m00();
    }

    private static float tanDown(Matrix4fc projection) {
        return (projection.m21() - 1.0f) / projection.m11();
    }

    private static float tanUp(Matrix4fc projection) {
        return (projection.m21() + 1.0f) / projection.m11();
    }
}

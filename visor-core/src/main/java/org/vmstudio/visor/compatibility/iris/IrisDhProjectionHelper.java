package org.vmstudio.visor.compatibility.iris;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;

public final class IrisDhProjectionHelper {

    private IrisDhProjectionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static void applyEyeFrustumShape(Matrix4f target) {
        Matrix4f eye = CapturedRenderingState.INSTANCE.getGbufferProjection();
        target.m00(eye.m00()).m11(eye.m11()).m20(eye.m20()).m21(eye.m21());
    }
}

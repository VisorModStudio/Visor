package org.vmstudio.visor.compatibility;


import org.vmstudio.visor.compatibility.shaders.IrisVRBridge;
import org.vmstudio.visor.compatibility.shaders.NoOpIrisVRBridge;

public final class ShadersHelper {
    private static volatile IrisVRBridge bridge = new NoOpIrisVRBridge();

    private ShadersHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void setBridge(IrisVRBridge newBridge) {
        bridge = (newBridge != null) ? newBridge : new NoOpIrisVRBridge();
    }

    public static IrisVRBridge bridge() {
        return bridge;
    }

    public static int shaderLight() {
        return bridge.getShaderLightValue();
    }

    public static boolean isShaderActive() {
        return bridge.isActive();
    }

    public static boolean sameSizedBuffers() {
        return bridge.sameSizedBuffers();
    }
}

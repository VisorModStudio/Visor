package org.vmstudio.visor.core.client.render;


import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.core.client.render.shaders.*;


public class VRShaders {

    @Getter
    private static VRShaderPostProcessEye postProcess;

    @Getter
    private static VRShaderMixedReality mixedReality;

    @Getter
    private static VRShaderTeleportPoint teleportPoint;

    @Getter
    private static VRShaderEndPortal endPortal;

    @Getter
    private static VRShaderInBlockVignette inBlockVignette;


    private VRShaders() {

    }

    public static void setup() throws Exception {
        postProcess = new VRShaderPostProcessEye();
        postProcess.init();
        GLUtils.checkGLError("init PostProcess shader");

        mixedReality = new VRShaderMixedReality();
        mixedReality.init();
        GLUtils.checkGLError("init MixedReality shader");

        teleportPoint = new VRShaderTeleportPoint();
        teleportPoint.init();
        GLUtils.checkGLError("init TeleportPoint shader");

        endPortal = new VRShaderEndPortal();
        endPortal.init();
        GLUtils.checkGLError("init EndPortal shader");

        inBlockVignette = new VRShaderInBlockVignette();
        inBlockVignette.init();
        GLUtils.checkGLError("init InBlockVignette shader");
    }


}

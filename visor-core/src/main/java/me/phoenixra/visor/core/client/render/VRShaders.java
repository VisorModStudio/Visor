package me.phoenixra.visor.core.client.render;


import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.core.client.render.shaders.VRShaderMixedReality;
import me.phoenixra.visor.core.client.render.shaders.VRShaderPostProcessEye;


public class VRShaders {

    @Getter
    private static VRShaderPostProcessEye postProcess;

    @Getter
    private static VRShaderMixedReality mixedReality;


    private VRShaders() {

    }

    public static void setup() throws Exception {
        postProcess = new VRShaderPostProcessEye();
        postProcess.init();
        GLUtils.checkGLError("init PostProcess shader");

        mixedReality = new VRShaderMixedReality();
        mixedReality.init();
        GLUtils.checkGLError("init MixedReality shader");
    }


}

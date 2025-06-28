package me.phoenixra.visor.core.client.render;


import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.core.client.render.shaders.VRShaderPostProcessEye;


public class VRShaders {

    @Getter
    private static VRShaderPostProcessEye postProcess;


    private VRShaders() {

    }

    public static void setup() throws Exception {
        postProcess = new VRShaderPostProcessEye();
        postProcess.init();
        GLUtils.checkGLError("init PostPRocess shader");
    }


}

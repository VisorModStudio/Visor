package me.phoenixra.visor.core.client.render;


import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.core.client.render.shaders.VRShaderPostProcess;


public class VRShaders {

    @Getter
    private static VRShaderPostProcess postProcess;


    private VRShaders() {

    }

    public static void setup() throws Exception {
        postProcess = new VRShaderPostProcess();
        postProcess.init();
        GLUtils.checkGLError("init PostPRocess shader");
    }


}

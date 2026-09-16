package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

public class VRShaderInBlockVignette implements VRShader {

    @Getter
    private McShaderProgram handle;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_in_block_vignette", DefaultVertexFormat.POSITION_TEX, true);
    }

    public void prepare(float proximity) {
        handle.setUniform("uInBlockProximity", proximity);
    }
}

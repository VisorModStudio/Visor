package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

public class VRShaderPumpkinOverlay implements VRShader {

    @Getter
    private McShaderProgram handle;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_pumpkin_overlay", DefaultVertexFormat.POSITION_TEX, true);
    }

    public void prepare(float opacity) {
        handle.setUniform("uOpacity", opacity);
    }
}

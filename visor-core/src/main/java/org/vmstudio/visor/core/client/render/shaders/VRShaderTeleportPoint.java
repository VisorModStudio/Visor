package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

public class VRShaderTeleportPoint implements VRShader{
    @Getter
    private McShaderProgram handle;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_teleport_point", DefaultVertexFormat.POSITION, true);
    }


    public McShaderProgram prepare(Matrix4f modelView,
                                   Matrix4f projection,
                                   float time,
                                   AtumColor color){
        handle.setModelViewMatrix(modelView);
        handle.setProjectionMatrix(projection);
        setUniforms(time, color);
        handle.apply();
        return handle;
    }

    // drawn with the engine's current matrices
    public McShaderProgram prepare(float time, AtumColor color) {
        setUniforms(time, color);
        handle.use();
        return handle;
    }

    private void setUniforms(float time, AtumColor color) {
        handle.setUniform("uTime", time);
        float[] normColor = new float[] {
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        };
        handle.setUniform("uColor", normColor);
    }


}

package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import org.joml.Matrix4f;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public class VRShaderTeleportPoint implements VRShader{

    public static final ShaderProgram PROGRAM = new ShaderProgram(
            McVersionUtils.newResourceLoc("core/vr_teleport_point"),
            DefaultVertexFormat.POSITION,
            ShaderDefines.EMPTY
    );

    @Getter
    private CompiledShaderProgram handle;

    public static AbstractUniform uTime;
    public static AbstractUniform uColor;

    @Override
    public void init() throws Exception {
        handle = VRShader.link(PROGRAM);

        uTime = handle.safeGetUniform("uTime");
        uColor = handle.safeGetUniform("uColor");
    }


    public CompiledShaderProgram prepare(Matrix4f modelView,
                                   Matrix4f projection,
                                   float time,
                                   AtumColor color){
        handle.MODEL_VIEW_MATRIX.set(
                modelView
        );
        handle.PROJECTION_MATRIX.set(
                projection
        );

        uTime.set(time);
        float[] normColor = new float[] {
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        };
        uColor.set(normColor);

        handle.apply();
        return handle;
    }


}

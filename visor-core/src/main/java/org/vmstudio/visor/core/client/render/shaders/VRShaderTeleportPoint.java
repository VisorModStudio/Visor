package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public class VRShaderTeleportPoint implements VRShader{
    @Getter
    private ShaderInstance handle;

    public static AbstractUniform uTime;
    public static AbstractUniform uColor;

    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
                "vr_teleport_point", DefaultVertexFormat.POSITION);

        uTime = handle.safeGetUniform("uTime");
        uColor = handle.safeGetUniform("uColor");
    }


    public ShaderInstance prepare(Matrix4f modelView,
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

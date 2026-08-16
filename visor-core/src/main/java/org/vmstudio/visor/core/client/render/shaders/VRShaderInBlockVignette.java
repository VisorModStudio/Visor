package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public class VRShaderInBlockVignette implements VRShader {

    public static final ShaderProgram PROGRAM = new ShaderProgram(
            McVersionUtils.newResourceLoc("core/vr_in_block_vignette"),
            DefaultVertexFormat.POSITION_TEX,
            ShaderDefines.EMPTY
    );

    @Getter
    private CompiledShaderProgram handle;


    private AbstractUniform uInBlockProximity;

    @Override
    public void init() throws Exception {
        handle = VRShader.link(PROGRAM);

        uInBlockProximity = handle.safeGetUniform("uInBlockProximity");
    }

    public void prepare(float proximity) {
        uInBlockProximity.set(proximity);
    }
}

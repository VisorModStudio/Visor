package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class VRShaderInBlockVignette implements VRShader {

    @Getter
    private ShaderInstance handle;


    private AbstractUniform uInBlockProximity;

    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(
                Minecraft.getInstance().getResourceManager(),
                "vr_in_block_vignette",
                DefaultVertexFormat.POSITION_TEX
        );
        uInBlockProximity = handle.safeGetUniform("uInBlockProximity");
    }

    public void prepare(float proximity) {
        uInBlockProximity.set(proximity);
    }
}
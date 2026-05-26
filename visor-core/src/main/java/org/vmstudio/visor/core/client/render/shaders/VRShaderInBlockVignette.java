package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class VRShaderInBlockVignette implements VRShader {

    @Getter
    private ShaderInstance handle;

    private AbstractUniform uEye;
    private AbstractUniform uInBlockProximity;

    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(
                Minecraft.getInstance().getResourceManager(),
                "vr_in_block_vignette",
                DefaultVertexFormat.POSITION_TEX
        );
        uEye = handle.safeGetUniform("uEye");
        uInBlockProximity = handle.safeGetUniform("uInBlockProximity");
    }

    public void prepare(EyeType eye, float proximity) {
        uEye.set(eye == EyeType.LEFT ? 1 : -1);
        uInBlockProximity.set(proximity);
    }
}
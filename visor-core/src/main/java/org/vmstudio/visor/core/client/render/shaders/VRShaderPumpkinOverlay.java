package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class VRShaderPumpkinOverlay implements VRShader {

    @Getter
    private ShaderInstance handle;

    private AbstractUniform uOpacity;

    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(
                Minecraft.getInstance().getResourceManager(),
                "vr_pumpkin_overlay",
                DefaultVertexFormat.POSITION_TEX
        );
        uOpacity = handle.safeGetUniform("uOpacity");
    }

    public void prepare(float opacity) {
        uOpacity.set(opacity);
    }
}

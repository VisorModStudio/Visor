package me.phoenixra.visor.core.client.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.core.client.render.helpers.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

@Getter
public class VRShaderPostProcess implements VRShader{



    private ShaderInstance handle;






    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "vr_post_process", DefaultVertexFormat.POSITION_TEX);

    }



    public void processEye(EyeType eye,
                           RenderTarget source,
                           float partialTick) {


        ShaderHelper.renderFullscreenQuad(handle, source);
    }
}

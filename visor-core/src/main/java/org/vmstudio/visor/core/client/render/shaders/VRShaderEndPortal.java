package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

public class VRShaderEndPortal implements VRShader{
    @Getter
    private McShaderProgram handle;
    @Getter
    private RenderType renderType;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_end_portal", DefaultVertexFormat.POSITION, true);

        renderType = createRenderType();
    }


    private RenderType createRenderType(){
        return handle.renderType(
                "end_portal",
                VertexFormat.Mode.QUADS,
                256,
                TheEndPortalRenderer.END_SKY_LOCATION,
                TheEndPortalRenderer.END_PORTAL_LOCATION);
    }
}

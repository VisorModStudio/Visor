package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.joml.Matrix3f;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;
import com.mojang.blaze3d.PrimitiveTopology;

public class VRShaderEndPortal implements VRShader{
    @Getter
    private McShaderProgram handle;
    @Getter
    private RenderType renderType;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_end_portal", DefaultVertexFormat.POSITION, true);
        // the json defaults, which 1.21.5 no longer reads
        handle.setUniform("EndPortalLayers", 15);
        handle.setUniform("IViewRotMat", new Matrix3f());

        renderType = createRenderType();
    }


    private RenderType createRenderType(){
        return handle.renderType(
                "end_portal",
                PrimitiveTopology.QUADS,
                256,
                TheEndPortalRenderer.END_SKY_LOCATION,
                TheEndPortalRenderer.END_PORTAL_LOCATION);
    }
}

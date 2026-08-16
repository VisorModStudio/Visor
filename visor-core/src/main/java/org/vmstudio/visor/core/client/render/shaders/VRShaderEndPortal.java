package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;

public class VRShaderEndPortal implements VRShader{

    public static final ShaderProgram PROGRAM = new ShaderProgram(
            McVersionUtils.newResourceLoc("core/vr_end_portal"),
            DefaultVertexFormat.POSITION,
            ShaderDefines.EMPTY
    );

    @Getter
    private CompiledShaderProgram handle;
    @Getter
    private RenderType renderType;

    @Override
    public void init() throws Exception {
        handle = VRShader.link(PROGRAM);

        renderType = createRenderType();
    }


    private RenderType createRenderType(){
        return RenderType
                .create(
                        "end_portal",
                        DefaultVertexFormat.POSITION,
                        VertexFormat.Mode.QUADS,
                        256,
                        false,
                        false,
                        RenderType.CompositeState.builder()
                                // 1.21.2: the shard takes the program handle, not a live supplier
                                .setShaderState(new RenderStateShard.ShaderStateShard(PROGRAM))
                                .setTextureState(
                                        RenderStateShard
                                                .MultiTextureStateShard
                                                .builder()
                                                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                                .build())
                                .createCompositeState(false));
    }
}

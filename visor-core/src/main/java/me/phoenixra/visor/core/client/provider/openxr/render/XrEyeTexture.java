package me.phoenixra.visor.core.client.provider.openxr.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.core.rendering.XRTexture;
import me.phoenixra.visor.api.client.render.VREyeTexture;

public class XrEyeTexture extends XRTexture implements VREyeTexture {
    @Getter
    private XrRenderTarget renderTarget;


    public XrEyeTexture(int width, int height, int textureId, int index) {
        super(width, height, textureId, index);
    }

    @Override
    public XrEyeTexture init() {
        renderTarget = new XrRenderTarget(
                width, height, textureId, textureIndex
        );
        return this;
    }

    @Override
    public String getName() {
        return "Eye target";
    }


    @Override
    public void destroy() {
        getRenderTarget().destroyBuffers();
    }
}

package org.vmstudio.visor.core.client.provider.openxr.render;

import lombok.Getter;
import me.phoenixra.atumvr.core.rendering.XRTexture;
import org.vmstudio.visor.api.client.render.VREyeTexture;

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

package org.vmstudio.visor.api.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.atumvr.api.rendering.AtumVRTexture;

public interface VREyeTexture extends AtumVRTexture {
    String getName();
    RenderTarget getRenderTarget();

}

package me.phoenixra.visor.api.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.atumvr.api.rendering.VRTexture;

public interface VREyeTexture extends VRTexture {
    String getName();
    RenderTarget getRenderTarget();

}

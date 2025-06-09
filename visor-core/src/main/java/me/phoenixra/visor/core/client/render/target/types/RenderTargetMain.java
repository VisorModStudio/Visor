package me.phoenixra.visor.core.client.render.target.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.mcmodified.render.RenderTargetModified;
import me.phoenixra.visor.core.client.render.target.RenderTargetHolder;
import me.phoenixra.visor.core.client.render.target.VRRenderTarget;
import net.minecraft.client.Minecraft;


public class RenderTargetMain implements RenderTargetHolder {
    @Getter
    private RenderTarget target;
    @Getter
    private RenderTarget mirrorTarget;

    @Override
    public void init(int width, int height) throws Exception {
        target = new VRRenderTarget(
                "Main VR",
                width, height,
                true,
                () -> -1,
                 true,
                true
        );
        GLUtils.checkGLError("Main VR target setup");
        VisorClientImpl.LOGGER.info(this.target.toString());

        this.mirrorTarget = new VRRenderTarget(
                "Mirror",
                Math.max(1, ClientContext.renderer.getMirrorWidth()),
                Math.max(1, ClientContext.renderer.getMirrorHeight()),
                false, () -> -1,
                false, false
        );
        GLUtils.checkGLError("Mirror VR target setup");
        VisorClientImpl.LOGGER.info(this.mirrorTarget.toString());




    }

    @Override
    public void resize(int width, int height) throws Exception {
        ((RenderTargetModified) target).visor$setUseStencil(
                true
        );
        target.resize(width, height, Minecraft.ON_OSX);
        this.mirrorTarget.resize(
                Math.max(1, ClientContext.renderer.getMirrorWidth()),
                Math.max(1, ClientContext.renderer.getMirrorHeight()),
                Minecraft.ON_OSX
        );

    }

    @Override
    public void destroy() {
        if(target != null) {
            target.destroyBuffers();
            target = null;
        }
        if(mirrorTarget != null) {
            mirrorTarget.destroyBuffers();
            mirrorTarget = null;
        }
    }
}

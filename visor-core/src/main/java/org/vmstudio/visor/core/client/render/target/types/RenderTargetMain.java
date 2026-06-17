package org.vmstudio.visor.core.client.render.target.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.extensions.client.render.RenderTargetExtension;
import org.vmstudio.visor.core.client.render.target.RenderTargetHolder;
import org.vmstudio.visor.core.client.render.target.VRRenderTarget;
import net.minecraft.client.Minecraft;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


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

        var mcWindow = (WindowExtension) (Object) MC.getWindow();
        this.mirrorTarget = new VRRenderTarget(
                "Mirror",
                mcWindow.visor$getActualScreenWidth(),
                mcWindow.visor$getActualScreenHeight(),
                true, () -> -1,
                false, false
        );
        GLUtils.checkGLError("Mirror VR target setup");
        VisorClientImpl.LOGGER.info(this.mirrorTarget.toString());




    }

    @Override
    public void resize(int width, int height) throws Exception {
        ((RenderTargetExtension) target).visor$setUseStencil(
                true
        );
        target.resize(width, height, Minecraft.ON_OSX);
        var mcWindow = (WindowExtension) (Object) MC.getWindow();
        this.mirrorTarget.resize(
                Math.max(1, mcWindow.visor$getActualScreenWidth()),
                Math.max(1, mcWindow.visor$getActualScreenHeight()),
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

package org.vmstudio.visor.core.client.render.target.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.render.target.RenderTargetHolder;
import org.vmstudio.visor.core.client.render.target.VRRenderTarget;
import net.minecraft.client.Minecraft;

//Third Person mirror
public class RenderTargetThird implements RenderTargetHolder {
    @Getter
    private RenderTarget target;

    @Override
    public void init(int width, int height) throws Exception {


        target = new VRRenderTarget("Third Person",
                width, height,
                true,
                () -> -1,
                true, false
        );

        GLUtils.checkGLError("Third Person target setup");
        VisorClientImpl.LOGGER.info(this.target.toString());


    }

    @Override
    public void resize(int width, int height) throws Exception {
        if(width < 1 || height < 1) return;
        if (this.target != null) {
            target.resize(width, height, Minecraft.ON_OSX);
        }
    }

    @Override
    public void destroy() {
        if (this.target != null) {
            target.destroyBuffers();
            target = null;
        }
    }

}

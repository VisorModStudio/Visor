package me.phoenixra.visor.core.client.render.target.impl;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.render.target.RenderTargetHolder;
import me.phoenixra.visor.core.client.render.target.VRRenderTarget;
import net.minecraft.client.Minecraft;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Getter
public class RenderTargetGUI implements RenderTargetHolder {
    private RenderTarget target = null;


    private int savedWidth;
    private int savedHeight;
    private boolean init;
    @Override
    public void init(int width, int height) throws Exception {
        target = new VRRenderTarget(
                "GUI",
                width, height,
                true,
                ()-> -1, true, false
        );
        GLUtils.checkGLError("GUI target setup");
        VisorClientImpl.LOGGER.info(target.toString());


        savedWidth = width;
        savedHeight = height;
        init = true;
    }

    @Override
    public void resize(int width, int height) throws Exception {
        target.resize(
                width, height,
                Minecraft.ON_OSX
        );
        if (MC.screen != null) {
            int screenWidth = MC.getWindow().getGuiScaledWidth();
            int screenHeight = MC.getWindow().getGuiScaledHeight();
            MC.screen.init(MC, screenWidth, screenHeight);
        }
        savedWidth = width;
        savedHeight = height;
    }

    @Override
    public void destroy() {
        if(target != null){
            target.destroyBuffers();
            target = null;
        }

        init = false;
    }

}

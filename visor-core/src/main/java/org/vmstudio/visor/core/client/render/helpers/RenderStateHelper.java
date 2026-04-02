package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11C;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderStateHelper {
    private RenderStateHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void restoreAfterExternalRender() {
        restoreAfterExternalRender(false);
    }

    public static void restoreAfterExternalRender(boolean keepStencilTest) {
        if (MC != null && MC.mainRenderTarget != null) {
            MC.mainRenderTarget.bindWrite(true);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        if (keepStencilTest) {
            GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        } else {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }
}

package org.vmstudio.visor.core.client.render.helpers;

import me.phoenixra.atumvr.api.utils.GLUtils;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.util.HashSet;
import java.util.Set;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderStateHelper {
    private static final Set<String> reportedExternalGLErrors = new HashSet<>();

    private RenderStateHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void drainExternalGLErrors(String site) {
        int error = GLUtils.drainGLErrors();
        if (error != 0 && reportedExternalGLErrors.add(site + '#' + error)) {
            VisorClientImpl.LOGGER.warn("OpenGL error {} left pending by earlier GL calls, cleared at {}", error, site);
        }
    }

    public static void restoreAfterExternalRender() {
        restoreAfterExternalRender(false);
    }

    public static void restoreAfterExternalRender(boolean keepStencilTest) {
        if (MC != null && McRenderTarget.mainTarget() != null) {
            McRenderTarget.bindWrite(McRenderTarget.mainTarget());
        }

        McGlState.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        McGlState.colorMask(true, true, true, true);
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11C.GL_LEQUAL);
        McGlState.depthMask(true);
        McGlState.enableCull();
        McGlState.defaultBlendFunc();
        McGlState.disableBlend();

        if (keepStencilTest) {
            GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        } else {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }
}

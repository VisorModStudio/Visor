package org.vmstudio.visor.compatibility.dh;

import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

public final class DhCompatHelper {
    public static final String MOD_ID = "distanthorizons";

    private DhCompatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isLoaded() {
        return ModLoader.get().isModLoaded(MOD_ID);
    }

    public static boolean shouldSkipDhRender() {
        if (VisorState.get().isNotActive()
                || VRRenderState.getPhase().isNotVRWorld()) {
            return false;
        }

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == null || !renderPass.isWorld() || renderPass.isEye()) {
            return false;
        }

        return !VRClientSettings.isDhMirrorPasses();
    }

    public static boolean isVrEyeWorldPass() {
        if (VisorState.get().isNotActive()
                || VRRenderState.getPhase().isNotVRWorld()) {
            return false;
        }

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        return renderPass != null && renderPass.isEye();
    }
}
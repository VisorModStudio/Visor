package org.vmstudio.visor.compatibility;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

public class LodPassHelper {
    @Nullable
    public static VRRenderPass currentWorldPass() {
        if (VisorState.get().isNotActive()
                || VRRenderState.getPhase().isNotVRWorld()) {
            return null;
        }

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        return renderPass != null && renderPass.isWorld() ? renderPass : null;
    }

    public static boolean isVrWorldPass() {
        return currentWorldPass() != null;
    }

    public static boolean isVrEyeWorldPass() {
        VRRenderPass renderPass = currentWorldPass();
        return renderPass != null && renderPass.isEye();
    }

    public static boolean shouldSkipMirrorPass() {
        VRRenderPass renderPass = currentWorldPass();
        if (renderPass == null || renderPass.isEye()) {
            return false;
        }

        return !VRClientSettings.isDhMirrorPasses();
    }
}

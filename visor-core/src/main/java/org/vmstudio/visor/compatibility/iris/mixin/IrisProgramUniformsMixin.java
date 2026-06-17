package org.vmstudio.visor.compatibility.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

/**
 * Resets Iris' frame cache when reusing {@code ProgramUniforms} across VR passes.
 * Forces a fresh upload of PER_FRAME uniforms per eye to prevent "IPD-offset world shimmer",
 * while remaining disabled in single-pipeline fallback to avoid breaking TAA history
 */
@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.gl.program.ProgramUniforms",
        "net.coderbot.iris.gl.program.ProgramUniforms"
}, remap = false)
public class IrisProgramUniformsMixin {
    @Shadow
    int lastFrame;

    @Unique
    private VRRenderPass visor$lastPass;

    @Inject(method = "update()V", at = @At("HEAD"))
    private void visor$reuploadPerFrameOnPassChange(CallbackInfo ci) {
        if (!IrisCompatHelper.perEyePipelines() || !VisorState.get().isActive()) {
            return;
        }
        VRRenderPass pass = VRRenderState.getRenderPass();
        if (pass != visor$lastPass) {
            visor$lastPass = pass;
            this.lastFrame = -1;
        }
    }
}

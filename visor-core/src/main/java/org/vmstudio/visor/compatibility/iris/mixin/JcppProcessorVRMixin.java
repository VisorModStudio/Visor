package org.vmstudio.visor.compatibility.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.vmstudio.visor.compatibility.shaders.ShaderPatcher;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.api.client.settings.VRClientSettings;

@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor",
        "net.coderbot.iris.shaderpack.preprocessor.JcppProcessor"
}, remap = false)
public class JcppProcessorVRMixin {
    @ModifyVariable(method = "glslPreprocessSource", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private static String visor$patchShaderSource(String source) {
        if (source != null && visor$shouldPatchForVr()) {
            return ShaderPatcher.patchShader(source);
        }
        return source;
    }

    @Unique
    private static boolean visor$shouldPatchForVr() {
        return VisorState.get().isInitialized()
                || VRClientSettings.getVrPlayMode().canInitVR();
    }
}
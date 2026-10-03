package org.vmstudio.visor.compatibility.voxy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.voxy.VoxyIrisPipelineBinder;

import java.util.List;

@Pseudo
@MixinGate(
        classes = "me.cortex.voxy.client.iris.IrisVoxyRenderPipelineData",
        methods = "createUniformSet"
)
@Mixin(targets = "me.cortex.voxy.client.iris.IrisVoxyRenderPipelineData", remap = false)
public class VoxyIrisPipelineDataVRMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "createUniformSet", at = @At("RETURN"), require = 0, expect = 0, remap = false)
    private static void visor$sameUniformLayoutForEveryPipeline(CallbackInfoReturnable<Object> cir) {
        if (cir.getReturnValue() instanceof List uniforms) {
            uniforms.sort(VoxyIrisPipelineBinder.UNIFORM_ORDER);
        }
    }
}

package org.vmstudio.visor.compatibility.voxy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.voxy.VoxyIrisPipelineBinder;

@Pseudo
@MixinGate(
        classes = {
                "me.cortex.voxy.client.core.IrisVoxyRenderPipeline",
                "me.cortex.voxy.client.iris.IGetIrisVoxyPipelineData",
                "net.irisshaders.iris.Iris"
        },
        methods = "preSetup",
        fields = {"data", "fbTranslucent"}
)
@Mixin(targets = "me.cortex.voxy.client.core.IrisVoxyRenderPipeline", remap = false)
public class VoxyIrisRenderPipelineVRMixin {
    @Inject(method = "preSetup", at = @At("HEAD"), require = 0, expect = 0, remap = false)
    private void visor$followIrisPipeline(CallbackInfo ci) {
        VoxyIrisPipelineBinder.rebindVoxyToIrisPipeline(this);
    }
}

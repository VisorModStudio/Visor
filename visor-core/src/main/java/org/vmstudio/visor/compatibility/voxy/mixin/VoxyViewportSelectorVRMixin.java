package org.vmstudio.visor.compatibility.voxy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.voxy.VoxyCompatHelper;

import java.util.Map;
import java.util.function.Supplier;

@Pseudo
@MixinGate(
        classes = "me.cortex.voxy.client.core.rendering.ViewportSelector",
        methods = "getViewport",
        fields = {"extraViewports", "creator"}
)
@Mixin(targets = "me.cortex.voxy.client.core.rendering.ViewportSelector", remap = false)
public class VoxyViewportSelectorVRMixin {
    @SuppressWarnings("rawtypes")
    @Shadow(remap = false)
    @Final
    private Map extraViewports;

    @SuppressWarnings("rawtypes")
    @Shadow(remap = false)
    @Final
    private Supplier creator;

    @SuppressWarnings("unchecked")
    @Inject(method = "getViewport", at = @At("RETURN"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$viewportPerPass(CallbackInfoReturnable<Object> cir) {
        VRRenderPass renderPass = VoxyCompatHelper.viewportPass();
        if (renderPass == null) {
            return;
        }

        Object chosen = cir.getReturnValue();
        if (chosen == null || this.extraViewports.containsValue(chosen)) {
            return;
        }

        cir.setReturnValue(this.extraViewports.computeIfAbsent(renderPass, __ -> this.creator.get()));
    }

}

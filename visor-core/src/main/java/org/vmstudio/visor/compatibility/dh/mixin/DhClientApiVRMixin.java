package org.vmstudio.visor.compatibility.dh.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;

@Pseudo
@ClassDependentMixin("com.seibel.distanthorizons.core.api.internal.ClientApi")
@Mixin(targets = "com.seibel.distanthorizons.core.api.internal.ClientApi", remap = false)
public class DhClientApiVRMixin {
    @Inject(method = "renderLods", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void visor$skipLodsForNonEyePass(CallbackInfo ci) {
        if (DhCompatHelper.shouldSkipDhRender()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderDeferredLodsForShaders", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void visor$skipDeferredLodsForNonEyePass(CallbackInfo ci) {
        if (DhCompatHelper.shouldSkipDhRender()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFadeOpaque", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$skipFadeOpaqueForNonEyePass(CallbackInfo ci) {
        if (DhCompatHelper.shouldSkipDhRender()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFade", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$skipFadeForNonEyePass(CallbackInfo ci) {
        if (DhCompatHelper.shouldSkipDhRender()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFadeTransparent", at = @At("HEAD"), cancellable = true, require = 0, expect = 0, remap = false)
    private void visor$skipFadeTransparentForNonEyePass(CallbackInfo ci) {
        if (DhCompatHelper.shouldSkipDhRender()) {
            ci.cancel();
        }
    }
}
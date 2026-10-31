package org.vmstudio.visor.compatibility.flashback.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.flashback.FlashbackCompatHelper;
import org.vmstudio.visor.core.client.VisorState;

@Pseudo
@MixinGate(classes = "com.moulberry.flashback.record.Recorder", methods = "endTick")
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public class FlashbackRecorderTickMixin {
    @Inject(method = "endTickWithContext", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void visor$deferEndTickWithContext(boolean close, CallbackInfo ci) {
        if (visor$defer(close, "endTickWithContext")) ci.cancel();
    }

    @Inject(method = "endTick", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void visor$deferEndTick(boolean close, CallbackInfo ci) {
        if (visor$defer(close, "endTick")) ci.cancel();
    }

    private boolean visor$defer(boolean close, String method) {
        if (FlashbackCompatHelper.isRunningDeferredEndTick()) return false;
        FlashbackCompatHelper.flushDeferredEndTick();
        if (close || !VisorState.get().isActive()) return false;
        return FlashbackCompatHelper.deferEndTick(this, method);
    }
}
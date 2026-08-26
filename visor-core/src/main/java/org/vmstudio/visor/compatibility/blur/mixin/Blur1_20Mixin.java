package org.vmstudio.visor.compatibility.blur.mixin;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;

// Works for 1.20.x (tterrag version, Blur)
@Pseudo
@MixinGate(classes = "com.tterrag.blur.Blur")
@Mixin(targets = "com.tterrag.blur.Blur", remap = false)
public class Blur1_20Mixin {
    @Shadow(remap = false)
    public static long start;

    @Inject(method = "onScreenChange", at = @At("HEAD"), cancellable = true, remap = false)
    private static void visor$skipBlurFade(Screen newScreen, CallbackInfo ci) {
        if (VisorState.get().isNotActive()) return;

        start = 0;
        ci.cancel();
    }

    @Inject(method = "getProgress", at = @At("HEAD"), cancellable = true, remap = false)
    private static void visor$dropBlurProgress(boolean fadeIn, CallbackInfoReturnable<Float> cir) {
        if (VisorState.get().isNotActive()) return;

        start = 0;
        cir.setReturnValue(fadeIn ? 1.0F : 0.0F);
    }
}

// #!MC-VERSION:: 1.21.5+
package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.systems.RenderSystem;
//? if >=26.1 {
import net.minecraft.client.FramerateLimiter;
//?}
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.helpers.ShaderTextureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
@Mixin(FramerateLimiter.class)
//?} else {
/*@Mixin(RenderSystem.class)
*///?}
public class RenderSystemMixin {

    @Inject(at = @At("HEAD"), method = "limitDisplayFPS",
            cancellable = true, remap = false)
    private static void visor$cancelFPSLimit(CallbackInfo ci) {
        if (VisorState.isVrFramePaced()) {
            ci.cancel();
        }
    }

}

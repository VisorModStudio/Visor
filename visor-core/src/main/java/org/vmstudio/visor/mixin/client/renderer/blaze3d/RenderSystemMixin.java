package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.vmstudio.visor.core.client.VisorState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(at = @At("HEAD"), method = "limitDisplayFPS",
            cancellable = true, remap = false)
    private static void visor$noFPSlimit(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "defaultBlendFunc", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V", remap = true), remap = false, index = 3)
    private static GlStateManager.DestFactor visor$defaultBlendFuncAlphaBlending(
            GlStateManager.DestFactor destFactor) {
        return GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA;
    }

}

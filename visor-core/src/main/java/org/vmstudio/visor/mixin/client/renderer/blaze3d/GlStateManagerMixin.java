package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.platform.GlStateManager;
import org.vmstudio.visor.core.client.render.helpers.ShaderTextureHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

    //game needs vanilla textures + VR, default limit is too short.
    @ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/stream/IntStream;range(II)Ljava/util/stream/IntStream;"), index = 1, method = "<clinit>")
    private static int visor$moreTextureUnitStates(int original) {
        return Math.max(original, 32);
    }

    // vanilla GUI blend zeroes dst alpha; keep it accumulating so the GUI layer composites correctly in VR
    @ModifyVariable(method = "_blendFuncSeparate", at = @At("HEAD"), remap = false, index = 3, argsOnly = true)
    private static int visor$keepGuiCoverage(int dstAlpha, int srcRgb, int dstRgb, int srcAlpha) {
        boolean vanillaGuiBlend = dstAlpha == GlStateManager.DestFactor.ZERO.value
                && srcAlpha == GlStateManager.SourceFactor.ONE.value
                && srcRgb == GlStateManager.SourceFactor.SRC_ALPHA.value
                && dstRgb == GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value;
        return vanillaGuiBlend
                ? GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value
                : dstAlpha;
    }

    @Inject(method = "_deleteTexture", at = @At("RETURN"), remap = false)
    private static void visor$forgetDeletedTexture(int texture, CallbackInfo ci) {
        ShaderTextureHelper.onTextureDeleted(texture);
    }

    @Inject(method = "_deleteTextures", at = @At("RETURN"), remap = false)
    private static void visor$forgetDeletedTextures(int[] textures, CallbackInfo ci) {
        for (int texture : textures) {
            ShaderTextureHelper.onTextureDeleted(texture);
        }
    }

    @Inject(method = "_genTexture", at = @At("RETURN"), remap = false)
    private static void visor$trackCreatedTexture(CallbackInfoReturnable<Integer> cir) {
        ShaderTextureHelper.onTextureCreated(cir.getReturnValue());
    }

    @Inject(method = "_genTextures", at = @At("RETURN"), remap = false)
    private static void visor$trackCreatedTextures(int[] textures, CallbackInfo ci) {
        for (int texture : textures) {
            ShaderTextureHelper.onTextureCreated(texture);
        }
    }
}

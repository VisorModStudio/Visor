// #!MC-VERSION:: 1.21.5+
package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL11;
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
        boolean vanillaGuiBlend = dstAlpha == GL11.GL_ZERO
                && srcAlpha == GL11.GL_ONE
                && srcRgb == GL11.GL_SRC_ALPHA
                && dstRgb == GL11.GL_ONE_MINUS_SRC_ALPHA;
        return vanillaGuiBlend
                ? GL11.GL_ONE_MINUS_SRC_ALPHA
                : dstAlpha;
    }

    @Inject(method = "_deleteTexture", at = @At("RETURN"), remap = false)
    private static void visor$forgetDeletedTexture(int texture, CallbackInfo ci) {
        ShaderTextureHelper.onTextureDeleted(texture);
    }

    @Inject(method = "_genTexture", at = @At("RETURN"), remap = false)
    private static void visor$trackCreatedTexture(CallbackInfoReturnable<Integer> cir) {
        ShaderTextureHelper.onTextureCreated(cir.getReturnValue());
    }

}

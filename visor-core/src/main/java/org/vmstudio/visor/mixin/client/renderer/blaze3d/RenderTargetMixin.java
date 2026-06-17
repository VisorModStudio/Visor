package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.TextureUtil;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.extensions.client.render.RenderTargetExtension;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;


@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {
    @Shadow
    public int frameBufferId;
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int viewHeight;
    @Shadow
    public int viewWidth;
    @Shadow
    protected int colorTextureId;


    @Unique
    private int visor$textureId = -1;
    @Unique
    private boolean visor$useLinearFilter;
    @Unique
    private boolean visor$useStencil = false;


    @Shadow
    public abstract void clear(boolean onMacIn);


    /* ************************* *\
  //--------STENCIL SUPPORT--------\\
    \* ************************* */
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 2)
    public int visor$vrUseStencil1(int internalformat) {
        return visor$useStencil
                ? GL30.GL_DEPTH24_STENCIL8
                : internalformat;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 6)
    public int visor$vrUseStencil2(int format) {
        return visor$useStencil
                ? GL30.GL_DEPTH_STENCIL : format;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 7)
    public int visor$vrUseStencil3(int type) {
        return visor$useStencil
                ? GL30.GL_UNSIGNED_INT_24_8 : type;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", remap = false, ordinal = 1), method = "createBuffers", index = 1)
    public int visor$vrUseStencil4(int attachment) {
        return visor$useStencil
                ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment;
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", remap = false, ordinal = 0), method = "createBuffers")
    public int visor$vrTextureId() {
        if (this.visor$textureId == -1) {
            return TextureUtil.generateTextureId();
        } else {
            return this.visor$textureId;
        }
    }

    @ModifyConstant(method = "createBuffers", constant = @Constant(intValue = 9728))
    public int visor$vrLinearFilter(int i) {
        return visor$useLinearFilter
                ? GL11.GL_LINEAR : i;
    }

    @Override
    public String toString() {
        String stringbuilder = "\n" +
                "Size:   " + this.viewWidth + " x " + this.viewHeight + "\n" +
                "FB ID:  " + this.frameBufferId + "\n" +
                "Tex ID: " + this.colorTextureId + "\n";
        return stringbuilder;
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    @Unique
    public void visor$setUseStencil(boolean useStencil) {
        this.visor$useStencil = useStencil;
    }

    @Override
    @Unique
    public boolean visor$isUsingStencil() {
        return visor$useStencil;
    }

    @Override
    @Unique
    public void visor$setTextureId(int texid) {
        this.visor$textureId = texid;
    }

    @Override
    @Unique
    public void visor$isLinearFilter(boolean linearFilter) {
        this.visor$useLinearFilter = linearFilter;
    }


}

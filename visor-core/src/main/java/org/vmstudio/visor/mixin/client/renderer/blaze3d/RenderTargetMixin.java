// #!MC-VERSION:: 1.21.5+
package org.vmstudio.visor.mixin.client.renderer.blaze3d;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
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
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int viewHeight;
    @Shadow
    public int viewWidth;


    @Unique
    private int visor$textureId = -1;
    @Unique
    private boolean visor$useLinearFilter;
    @Unique
    private boolean visor$useStencil = false;


    //--------STENCIL SUPPORT--------

    @Override
    public String toString() {
        return "\nSize:   " + this.viewWidth + " x " + this.viewHeight + "\n";
    }


    //--------PUBLIC METHODS--------

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
    public void visor$setLinearFilter(boolean linearFilter) {
        this.visor$useLinearFilter = linearFilter;
    }


}

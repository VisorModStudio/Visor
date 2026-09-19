package org.vmstudio.visor.mixin.client.accessors;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    //? if <1.21.5 {
    /*@Accessor
    static int[] getShaderTextures() {
        return null;
    }
    *///?}

    //? if <1.21.6 {
    /*@Accessor
    static void setShaderGameTime(float shaderGameTime) {
    }
    *///?}
}

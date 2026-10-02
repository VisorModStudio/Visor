// #!MC-VERSION:: 26.2+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.SkyRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @ModifyExpressionValue(
            method = {"renderSkyDisc", "renderDarkDisc", "renderSun", "renderMoon", "renderStars",
                    "renderSunriseAndSunset", "renderEndSky", "renderEndFlash"},
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"),
            require = 1)
    private RenderTarget visor$drawSkyToMainTarget(RenderTarget created) {
        RenderTarget main = McRenderTarget.mainTarget();
        return main != null ? main : created;
    }
}

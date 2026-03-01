package org.vmstudio.visor.mixin.client.renderer.blockentity;

import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRShaders;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EndPortalRendererMixins {



    @Mixin(TheEndGatewayRenderer.class)
    public static class EndGateway {

        @Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
        private void visor$overrideShader(CallbackInfoReturnable<RenderType> cir) {
            if (VRRenderState.getPhase().isNotVanilla()) {
                cir.setReturnValue(VRShaders.getEndPortal().getRenderType());
            }
        }
    }


    @Mixin(TheEndPortalRenderer.class)
    public static class EndPortal {

        @Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
        private void visor$overrideShader(CallbackInfoReturnable<RenderType> cir) {
            if (VRRenderState.getPhase().isNotVanilla()) {
                cir.setReturnValue(VRShaders.getEndPortal().getRenderType());
            }
        }
    }
}

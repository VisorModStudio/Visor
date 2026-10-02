// #!MC-VERSION:: 26.1.2+
package org.vmstudio.visor.mixin.client.renderer.blockentity;

import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.VRShaders;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EndPortalRendererMixins {



    @Mixin(TheEndGatewayRenderer.class)
    public static class EndGateway {

        @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EndGatewayRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;endGateway()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
        private RenderType visor$overrideShader(Operation<RenderType> original) {
            if (VRRenderState.getPhase().isNotVanilla()) {
                return VRShaders.getEndPortal().getRenderType();
            }
            return original.call();
        }
    }


    @Mixin(TheEndPortalRenderer.class)
    public static class EndPortal {

        @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EndPortalRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;endPortal()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
        private RenderType visor$overrideShader(Operation<RenderType> original) {
            if (VRRenderState.getPhase().isNotVanilla()) {
                return VRShaders.getEndPortal().getRenderType();
            }
            return original.call();
        }
    }
}

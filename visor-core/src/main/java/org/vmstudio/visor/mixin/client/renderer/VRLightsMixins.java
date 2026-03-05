package org.vmstudio.visor.mixin.client.renderer;

import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class VRLightsMixins {
    @Mixin(ClientLevel.class)
    public static class ClientLevelMixin {


        /**
         * Only process this when rendering vanilla
         * or VR camera that is a worldUpdater
         */
        @Inject(at = @At("HEAD"), method = "pollLightUpdates", cancellable = true)
        public void visor$noUpdateOncePerFrame(CallbackInfo info){
            if(VisorState.get().isNotActive()) return;
            if (VRRenderState.getRenderPass() != VRRenderPass.worldUpdater()) {
                info.cancel();
            }
        }
    }

    @Mixin(LevelLightEngine.class)
    public abstract static class LevelLightEngineMixin {

        @Unique
        private boolean visor$redirect;

        @Shadow
        public abstract int runLightUpdates();

        /**
         * Only process this when rendering vanilla
         * or VR camera that is a worldUpdater
         */
        @Inject(at = @At("HEAD"), method = "runLightUpdates", cancellable = true)
        public void visor$noUpdateOncePerFrame(CallbackInfoReturnable<Integer> callbackInfo){
            if(VisorState.get().isNotActive()) return;
            if(!visor$redirect) return;
            if (VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
                visor$redirect = false;
                callbackInfo.setReturnValue(runLightUpdates());
                visor$redirect = true;
            } else {
                callbackInfo.setReturnValue(0);
            }
        }
    }

}

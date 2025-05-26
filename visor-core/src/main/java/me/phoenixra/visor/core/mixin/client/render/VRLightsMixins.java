package me.phoenixra.visor.core.mixin.client.render;

import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.render.VRRenderState;
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
         * Canceled if called from non-tick display
         * @param info s
         */
        @Inject(at = @At("HEAD"), method = "pollLightUpdates", cancellable = true)
        public void visor$noUpdateIfNotTickDisplay(CallbackInfo info){
            if(VisorState.getStateMode().isNotActive()) return;
            if (VRRenderState.getCurrentVRDisplay() != VRDisplay.worldUpdater()) {
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
         * Canceled if called from non-tick display
         * @param callbackInfo s
         */
        @Inject(at = @At("HEAD"), method = "runLightUpdates", cancellable = true)
        public void visor$noUpdateIfNotTickDisplay(CallbackInfoReturnable<Integer> callbackInfo){
            if(VisorState.getStateMode().isNotActive()) return;
            if(!visor$redirect) return;
            if (VRRenderState.getCurrentVRDisplay() == VRDisplay.worldUpdater()) {
                visor$redirect = false;
                callbackInfo.setReturnValue(runLightUpdates());
                visor$redirect = true;
            } else {
                callbackInfo.setReturnValue(0);
            }
        }
    }

}

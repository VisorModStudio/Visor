package org.vmstudio.visor.mixin.client.renderer;

import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LevelRenderer.class)
public class NoSodiumLevelRendererMixin {

    @Shadow
    private boolean needsFullRenderChunkUpdate;
    @Shadow @Final
    private AtomicBoolean needsFrustumUpdate;

    @Inject(method = "setupRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;needsFullRenderChunkUpdate:Z", ordinal = 1, shift = At.Shift.AFTER))
    private void visor$alwaysUpdateCull(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            // fixes chunks cull frustum between displays
            this.needsFullRenderChunkUpdate = true;
            this.needsFrustumUpdate.set(true);
        }
    }
}

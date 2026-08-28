package org.vmstudio.visor.mixin.client.renderer;

import org.vmstudio.visor.compatibility.sodium.SodiumHelper;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

//Higher than Sodium priority
@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererCullMixin {

    @Shadow
    private boolean needsFullRenderChunkUpdate;
    @Shadow @Final
    private AtomicBoolean needsFrustumUpdate;

    @Inject(method = "setupRender", at = @At("HEAD"))
    private void visor$refreshCullingEachPass(CallbackInfo ci) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return;
        }
        // each VR pass has its own camera,
        // visibility cached by the previous pass won't work
        this.needsFullRenderChunkUpdate = true;
        this.needsFrustumUpdate.set(true);
    }
}

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

// 1.20.2 moved both culling flags into SectionOcclusionGraph
//? if >=26.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.extract.LevelExtractor;

//Higher than Sodium priority
@Mixin(value = LevelExtractor.class, priority = 1100)
public class LevelRendererCullMixin {

    @Shadow @Final
    private LevelRenderer levelRenderer;

    @Inject(method = "extract", at = @At("HEAD"))
    private void visor$refreshCullingEachPass(CallbackInfo ci) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return;
        }
        // each VR pass has its own camera,
        // visibility cached by the previous pass won't work
        this.levelRenderer.sectionOcclusionGraph().invalidate();
    }

    // needsFrustumUpdate is private to SectionOcclusionGraph now, so force the read instead of the write
    @ModifyExpressionValue(
            method = "extract",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;consumeFrustumUpdate()Z"))
    private boolean visor$forceFrustumUpdate(boolean original) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return original;
        }
        return true;
    }
}
//?} elif >=1.21.9 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.SectionOcclusionGraph;

//Higher than Sodium priority
@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererCullMixin {

    @Shadow @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Inject(method = "cullTerrain", at = @At("HEAD"))
    private void visor$refreshCullingEachPass(CallbackInfo ci) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return;
        }
        // each VR pass has its own camera,
        // visibility cached by the previous pass won't work
        this.sectionOcclusionGraph.invalidate();
    }

    // needsFrustumUpdate is private to SectionOcclusionGraph now, so force the read instead of the write
    @ModifyExpressionValue(
            method = "cullTerrain",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;consumeFrustumUpdate()Z"))
    private boolean visor$forceFrustumUpdate(boolean original) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return original;
        }
        return true;
    }
}
*///?} elif >=1.20.2 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.SectionOcclusionGraph;

//Higher than Sodium priority
@Mixin(value = LevelRenderer.class, priority = 1100)
public class LevelRendererCullMixin {

    @Shadow @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Inject(method = "setupRender", at = @At("HEAD"))
    private void visor$refreshCullingEachPass(CallbackInfo ci) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return;
        }
        // each VR pass has its own camera,
        // visibility cached by the previous pass won't work
        this.sectionOcclusionGraph.invalidate();
    }

    // needsFrustumUpdate is private to SectionOcclusionGraph now, so force the read instead of the write
    @ModifyExpressionValue(
            method = "setupRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;consumeFrustumUpdate()Z"))
    private boolean visor$forceFrustumUpdate(boolean original) {
        if (!VisorState.get().isActive() || SodiumHelper.isLoaded()) {
            return original;
        }
        return true;
    }
}
*///?} else {
/*import java.util.concurrent.atomic.AtomicBoolean;

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
*///?}

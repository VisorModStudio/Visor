package org.vmstudio.visor.compatibility.nvidium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.nvidium.NvidiumHelper;

@Pseudo
@Mixin(targets = "me.cortex.nvidium.RenderPipeline", remap = false)
public class RenderPipelineMixin {
    @Shadow(remap = false)
    private int prevRegionCount;

    @Unique
    private boolean visor$keepCommands;

    @Unique
    private int visor$savedRegionCount;

    @Inject(method = "renderFrame", at = @At("HEAD"), remap = false)
    private void visor$captureCommandOwner(CallbackInfo ci) {
        visor$keepCommands = NvidiumHelper.shouldKeepCommandBuffer();
        visor$savedRegionCount = this.prevRegionCount;
    }

    @ModifyArg(
            method = "renderFrame",
            at = @At(value = "INVOKE",
                    target = "Lme/cortex/nvidium/renderers/SectionRasterizer;raster(I)V"),
            remap = false
    )
    private int visor$skipCommandRebuild(int visibleRegions) {
        return visor$keepCommands ? 0 : visibleRegions;
    }

    @ModifyArg(
            method = "renderFrame",
            at = @At(value = "INVOKE",
                    target = "Lme/cortex/nvidium/renderers/TemporalTerrainRasterizer;raster(IJ)V"),
            index = 0,
            remap = false
    )
    private int visor$skipTemporalRebuild(int visibleRegions) {
        return visor$keepCommands ? 0 : visibleRegions;
    }

    @Inject(method = "renderFrame", at = @At("RETURN"), remap = false)
    private void visor$restoreCommandOwner(CallbackInfo ci) {
        if (visor$keepCommands) {
            this.prevRegionCount = visor$savedRegionCount;
        }
    }
}

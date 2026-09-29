package org.vmstudio.visor.loader.forge.mixin;

import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.6 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.loader.forge.ForgeModLoader;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}

//? if >=1.21.6 {
@Mixin(ChunkSectionsToRender.class)
public class ForgeChunkSectionsStageMixin {

    // 1.21.11 added the terrain GpuSampler parameter
    @Inject(method = "renderGroup", at = @At("TAIL"))
    private void visor$afterTranslucent(CallbackInfo ci, @Local(argsOnly = true) ChunkSectionLayerGroup group) {
        if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            ((ForgeModLoader) ModLoader.get()).fireLevelStage(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }
}
//?} else {
/*@Mixin(LevelRenderer.class)
public class ForgeChunkSectionsStageMixin {
}
*///?}

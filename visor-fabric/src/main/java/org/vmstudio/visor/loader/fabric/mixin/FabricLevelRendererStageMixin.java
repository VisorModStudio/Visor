package org.vmstudio.visor.loader.fabric.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.9 {
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.loader.fabric.FabricModLoader;
//?}
//? if >=1.21.9 && <26.3 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
*///?}
//? if >=1.21.11 && <26.3 {
/*import com.mojang.blaze3d.textures.GpuSampler;
*///?}

@Mixin(LevelRenderer.class)
public class FabricLevelRendererStageMixin {

    //? if >=26.2 {
    @Inject(method = "render", at = @At("TAIL"))
    private void visor$afterWorld(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_WORLD);
    }
    //?} elif >=1.21.9 {
    /*@Inject(method = "submitEntities", at = @At("HEAD"))
    private void visor$afterSolid(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_SOLID);
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void visor$afterWorld(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_WORLD);
    }
    *///?}

    //? if >=26.2 && <26.3 {
    /*@WrapOperation(method = "lambda$addMainPass$0", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"))
    private void visor$afterChunkGroup(ChunkSectionsToRender sections, ChunkSectionLayerGroup group,
                                       GpuSampler sampler, Operation<Void> original) {
        original.call(sections, group, sampler);
        if (group == ChunkSectionLayerGroup.OPAQUE) {
            visor$fire(RenderPipelineStage.AFTER_SOLID);
        } else if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            visor$fire(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }
    *///?} elif >=26.1 && <26.2 {
    /*@WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"))
    private void visor$afterChunkGroup(ChunkSectionsToRender sections, ChunkSectionLayerGroup group,
                                       GpuSampler sampler, Operation<Void> original) {
        original.call(sections, group, sampler);
        if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            visor$fire(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }
    *///?} elif >=1.21.11 && <26.1 {
    /*@WrapOperation(method = "method_62214", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"))
    private void visor$afterChunkGroup(ChunkSectionsToRender sections, ChunkSectionLayerGroup group,
                                       GpuSampler sampler, Operation<Void> original) {
        original.call(sections, group, sampler);
        if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            visor$fire(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }
    *///?} elif >=1.21.9 && <1.21.11 {
    /*@WrapOperation(method = "method_62214", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;)V"))
    private void visor$afterChunkGroup(ChunkSectionsToRender sections, ChunkSectionLayerGroup group,
                                       Operation<Void> original) {
        original.call(sections, group);
        if (group == ChunkSectionLayerGroup.TRANSLUCENT) {
            visor$fire(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }
    *///?}

    //? if >=1.21.9 {
    @Unique
    private static void visor$fire(RenderPipelineStage stage) {
        ((FabricModLoader) ModLoader.get()).fireLevelStage(stage);
    }
    //?}
}

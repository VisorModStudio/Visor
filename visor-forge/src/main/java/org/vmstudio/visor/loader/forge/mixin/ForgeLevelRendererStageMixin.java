package org.vmstudio.visor.loader.forge.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.2 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.loader.forge.ForgeModLoader;
//?}

@Mixin(LevelRenderer.class)
public class ForgeLevelRendererStageMixin {

    //? if >=1.21.9 {
    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void visor$afterSolid(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_SOLID);
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void visor$afterWorld(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_WORLD);
    }

    @Unique
    private static void visor$fire(RenderPipelineStage stage) {
        ((ForgeModLoader) ModLoader.get()).fireLevelStage(stage);
    }
    //?} elif >=1.21.6 {
    /*@Inject(method = "renderEntities", at = @At("HEAD"))
    private void visor$afterSolid(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_SOLID);
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void visor$afterWorld(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_WORLD);
    }

    @Unique
    private static void visor$fire(RenderPipelineStage stage) {
        ((ForgeModLoader) ModLoader.get()).fireLevelStage(stage);
    }
    *///?} elif >=1.21.2 {
    /*@Inject(method = "renderEntities", at = @At("HEAD"))
    private void visor$afterSolid(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_SOLID);
    }

    @Inject(method = "renderSectionLayer", at = @At("TAIL"))
    private void visor$afterTranslucent(CallbackInfo ci, @Local(argsOnly = true) RenderType renderType) {
        if (renderType == RenderType.translucent()) {
            visor$fire(RenderPipelineStage.AFTER_TRANSLUCENT);
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void visor$afterWorld(CallbackInfo ci) {
        visor$fire(RenderPipelineStage.AFTER_WORLD);
    }

    @Unique
    private static void visor$fire(RenderPipelineStage stage) {
        ((ForgeModLoader) ModLoader.get()).fireLevelStage(stage);
    }
    *///?}
}

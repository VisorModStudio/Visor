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

@Mixin(LevelRenderer.class)
public class FabricLevelRendererStageMixin {

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
        ((FabricModLoader) ModLoader.get()).fireLevelStage(stage);
    }
    //?}
}

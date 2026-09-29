package org.vmstudio.visor.mixin.client.renderer;

import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.6 {
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.VRRenderState;
//?} else {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?}

//? if >=1.21.6 {
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {

    @Shadow
    @Final
    private MappableRingBuffer ubo;

    @Inject(method = "render", at = @At("HEAD"))
    private void visor$cloudInfoPerPass(CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            this.ubo.rotate();
        }
    }
}
//?} else {
/*@Mixin(LevelRenderer.class)
public class CloudRendererMixin {
}
*///?}

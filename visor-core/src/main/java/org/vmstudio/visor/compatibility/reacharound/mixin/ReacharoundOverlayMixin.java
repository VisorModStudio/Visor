package org.vmstudio.visor.compatibility.reacharound.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.reacharound.ReacharoundRenderCompat;

@Pseudo
@Mixin(targets = "com.spanser.reacharound.client.gui.Overlay", remap = false)
@MixinGate(classes = "com.spanser.reacharound.client.gui.Overlay")
public class ReacharoundOverlayMixin {
    @Unique
    private boolean visor$previewSubmitted;

    @Inject(method = "render", at = @At("HEAD"), require = 0, remap = false)
    private void visor$beginRender(CallbackInfo ci) {
        visor$previewSubmitted = false;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitCustomGeometry(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V",
                    shift = At.Shift.AFTER,
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void visor$afterFilledPreviewSubmit(CallbackInfo ci) {
        visor$previewSubmitted = true;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitShapeOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/client/renderer/RenderType;IFZ)V",
                    shift = At.Shift.AFTER,
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void visor$afterOutlinePreviewSubmit(CallbackInfo ci) {
        visor$previewSubmitted = true;
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0, remap = false)
    private void visor$flushPreview(@Coerce Object context, CallbackInfo ci) {
        if (visor$previewSubmitted) {
            ReacharoundRenderCompat.flushPreviewBuffer(context);
            visor$previewSubmitted = false;
        }
    }
}

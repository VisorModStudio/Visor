package me.phoenixra.visor.mixin.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.visor.modified.client.render.RenderTargetModified;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.target.MultiCameraRenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.EnumMap;

@Mixin(PostChain.class)
public class PostChainMixin {

    @Shadow
    @Final
    private RenderTarget screenTarget;

    @Unique @Final
    private final EnumMap<VRCameraType, PostChain> visor$vrPostChains = new EnumMap<>(VRCameraType.class);


    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void visor$onInit(TextureManager textureManager,
                              ResourceManager resourceManager,
                              RenderTarget screenTarget,
                              ResourceLocation name,
                              CallbackInfo ci) throws IOException {

        if (VisorState.get().isNotInitialized()
                || this.screenTarget != VRRenderState.getVanillaTarget()){
            return;
        }
        for (VRCameraType cameraType : VRCameraType.values()) {
            var target = VRRenderState.getTargetForCamera(cameraType);
            if(target == null) continue;
            visor$vrPostChains.put(cameraType,
                    new PostChain(
                            textureManager,
                            resourceManager,
                            target,
                            name
                    )
            );
        }
    }

    @Inject(method = "process", at = @At(value = "HEAD"), cancellable = true)
    private void visor$processVRChains(float partialTick, CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVRWorld()){
            return;
        }
        PostChain vrChain = this.visor$vrPostChains.get(VRRenderState.getCameraType());
        if(vrChain == null){
            return;
        }
        vrChain.process(partialTick);
        ci.cancel();
    }


    @Inject(method = "getTempTarget", at = @At("RETURN"), cancellable = true)
    private void visor$onGetTempTarget(String attributeName, CallbackInfoReturnable<RenderTarget> cir) {
        if (VisorState.get().isNotInitialized()
                || visor$vrPostChains.isEmpty()) {
            return;
        }
        var vrTempTargets = new EnumMap<VRCameraType, RenderTarget>(VRCameraType.class);
        visor$vrPostChains.forEach((d, pc) -> {
            vrTempTargets.put(d, pc.getTempTarget(attributeName));
        });
        cir.setReturnValue(
                new MultiCameraRenderTarget(
                        cir.getReturnValue(), vrTempTargets
                )
        );
    }

    @ModifyVariable(method = "addTempTarget", at = @At(value = "STORE"), ordinal = 0)
    private RenderTarget visor$tempTargetStencil(RenderTarget renderTarget) {
        if (((RenderTargetModified) this.screenTarget).visor$isUsingStencil()) {
            ((RenderTargetModified) renderTarget).visor$setUseStencil(true);
            renderTarget.resize(renderTarget.width, renderTarget.height, Minecraft.ON_OSX);
        }
        return renderTarget;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void visor$onClose(CallbackInfo ci) {
        visor$vrPostChains.values().forEach(PostChain::close);
        visor$vrPostChains.clear();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void visor$onResize(CallbackInfo ci) {
        visor$vrPostChains.forEach((cameraType, pc) -> {
            RenderTarget target = VRRenderState.getTargetForCamera(cameraType);
            if(target == null){
                return;
            }
            pc.resize(target.width, target.height);
        });
    }

}

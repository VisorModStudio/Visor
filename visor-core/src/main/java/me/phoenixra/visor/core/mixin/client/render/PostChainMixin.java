package me.phoenixra.visor.core.mixin.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.visor.core.client.mcmodified.render.RenderTargetModified;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.target.MultiDisplayRenderTarget;
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
    private final EnumMap<VRDisplay, PostChain> visor$vrChains = new EnumMap<>(VRDisplay.class);

    @Unique @Final
    private final EnumMap<VRDisplay, RenderTarget> visor$vrTempTargets = new EnumMap<>(VRDisplay.class);


    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void visor$onInit(TextureManager textureManager,
                              ResourceManager resourceManager,
                              RenderTarget screenTarget,
                              ResourceLocation name,
                              CallbackInfo ci) throws IOException {

        if (VisorState.getStateMode().isNotInitialized()
                || this.screenTarget != VRRenderState.getVanillaTarget()){
            return;
        }
        for (VRDisplay display : VRDisplay.values()) {
            visor$vrChains.put(display,
                    new PostChain(
                            textureManager,
                            resourceManager,
                            VRRenderState.getTargetForDisplay(display),
                            name
                    )
            );
        }
    }

    @Inject(method = "process", at = @At(value = "HEAD"), cancellable = true)
    private void visor$processVRChains(float partialTick, CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isNotVRWorld()){
            return;
        }
        PostChain vrChain = this.visor$vrChains.get(VRRenderState.getCurrentVRDisplay());
        if(vrChain == null){
            return;
        }
        vrChain.process(partialTick);
        ci.cancel();
    }


    @Inject(method = "getTempTarget", at = @At("RETURN"), cancellable = true)
    private void visor$onGetTempTarget(String attributeName, CallbackInfoReturnable<RenderTarget> cir) {
        if (VisorState.getStateMode().isNotInitialized()
                || visor$vrChains.isEmpty()) return;
        visor$vrTempTargets.clear();
        visor$vrChains.forEach((d, pc) -> {
            visor$vrTempTargets.put(d, pc.getTempTarget(attributeName));
        });
        cir.setReturnValue(
                new MultiDisplayRenderTarget(
                        cir.getReturnValue(), visor$vrTempTargets
                )
        );
    }

    @ModifyVariable(method = "addTempTarget", at = @At(value = "STORE"), ordinal = 0)
    private RenderTarget visor$tempTargetStencil(RenderTarget renderTarget) {
        if (((RenderTargetModified) this.screenTarget).visor$isUseStencil()) {
            ((RenderTargetModified) renderTarget).visor$setUseStencil(true);
            renderTarget.resize(renderTarget.width, renderTarget.height, Minecraft.ON_OSX);
        }
        return renderTarget;
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void visor$onClose(CallbackInfo ci) {
        visor$vrChains.values().forEach(PostChain::close);
        visor$vrChains.clear();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void visor$onResize(CallbackInfo ci) {
        visor$vrChains.forEach((display, pc) -> {
            RenderTarget target = VRRenderState.getTargetForDisplay(display);
            pc.resize(target.width, target.height);
        });
    }

}

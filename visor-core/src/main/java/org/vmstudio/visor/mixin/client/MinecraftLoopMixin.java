// #!MC-VERSION:: 26.1.2+
package org.vmstudio.visor.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.mojang.blaze3d.platform.Window;
import org.vmstudio.visor.core.client.player.VRAimPicker;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskTeleport;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.context.PreRenderContext;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.extensions.client.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
//? if >=26.2 {
import com.mojang.renderpearl.api.device.GpuSurface;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyArg;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

// game VR loop
@Mixin(Minecraft.class)
public abstract class MinecraftLoopMixin implements MinecraftExtension {

    // ---- Unique fields ----
    @Unique
    private boolean visor$loopStartedByRunTick;
    @Unique
    private boolean visor$levelInVanillaFrame;

    // ---- Shadow fields ----
    //? if >=26.2 {
    @Shadow @Final
    private GpuSurface windowSurface;
    //?}


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    /**
     * Pre Ticks Visor right before mc tick() is called
     *
     * @param ci s
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V"), method = "runTick", require = 1)
    public void visor$preTick(CallbackInfo ci) {
        if(ClientContext.visor != null) {
            ClientContext.visor.preTickVR();
        }
    }

    /**
     * Ticks Visor (before mc tick methods called)
     *
     * @param info s
     */
    @Inject(at = @At("HEAD"), method = "tick()V", require = 1)
    public void visor$tick(CallbackInfo info) {
        if(ClientContext.visor != null) {
            ClientContext.visor.tickVR();
        }
    }

    /**
     * Post Ticks Visor right after mc tick() is called
     *
     * @param ci s
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER), method = "runTick", require = 1)
    public void visor$postTick(CallbackInfo ci) {
        if(ClientContext.visor != null) {
            ClientContext.visor.postTickVR();
        }
    }

    /**
     * Calls pre render task at the beginning of a frame
     *
     * @param tick     s
     * @param callback s
     */
    @Inject(at = @At("HEAD"), method = "runTick(Z)V", require = 1)
    public void visor$runVR(boolean tick, CallbackInfo callback) {
        visor$startGameLoop();
        visor$loopStartedByRunTick = true;
    }

    @Unique
    private void visor$startGameLoop() {
        VisorState.updateState();
        if (VisorState.get().isInitialized()) {
            McRenderTarget.setMainTarget(VRRenderState.getVanillaTarget());
        }
        if(ClientContext.visor != null) {
            ClientContext.visor
                    .onGameLoopStart();
        }
    }

    @Inject(method = "renderFrame", at = @At("HEAD"), require = 1)
    public void visor$preRenderVR(boolean tick, CallbackInfo callback) {
        //? if >=26.2 {
        if (this.windowSurface.isAcquired()) {
            return;
        }
        //?}
        if (!visor$loopStartedByRunTick) {
            visor$startGameLoop();
        }
        visor$loopStartedByRunTick = false;
        if(ClientContext.visor != null) {
            ClientContext.visor
                    .preRenderVR(
                            new PreRenderContext(
                                    McVersionClientUtils.profiler(), tick,
                                    visor$getPartialTicks()
                            )
                    );
        }
    }

    /**
     * Wraps vanilla GameRenderer.render() call
     * to update renderer state and start VRGui phase instead
     *
     * @param renderLevel s
     */
    //? if >=26.2 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;update(Lnet/minecraft/client/DeltaTracker;)V"), method = "renderFrame", require = 1)
    public void visor$startVRGuiPhase(GameRenderer instance, DeltaTracker deltaTracker, Operation<Void> original,
                                      @Local(argsOnly = true) boolean renderLevel) {
        visor$levelInVanillaFrame = renderLevel;
        if (VisorState.get().isActive()) {
            ClientContext.renderer.onGameRenderStart(renderLevel);
            //disabled in VRGui phase, fallback on exception
            visor$levelInVanillaFrame = renderLevel && !VRRenderState.getPhase().isVRGui();
        }
        original.call(instance, deltaTracker);
    }
    //?} else {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;update(Lnet/minecraft/client/DeltaTracker;Z)V"), method = "renderFrame", require = 1)
    public void visor$startVRGuiPhase(GameRenderer instance, DeltaTracker deltaTracker, boolean renderLevel, Operation<Void> original) {
        visor$levelInVanillaFrame = renderLevel;
        if (VisorState.get().isActive()) {
            ClientContext.renderer.onGameRenderStart(renderLevel);
            //disabled in VRGui phase, fallback on exception
            visor$levelInVanillaFrame = renderLevel && !VRRenderState.getPhase().isVRGui();
        }
        original.call(instance, deltaTracker, visor$levelInVanillaFrame);
    }
    *///?}

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"), method = "renderFrame", require = 1)
    public void visor$extractVRGuiPhase(GameRenderer instance, DeltaTracker deltaTracker, boolean renderLevel, Operation<Void> original) {
        original.call(instance, deltaTracker, visor$levelInVanillaFrame);
    }

    //? if >=26.3 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render()V"), method = "renderFrame", require = 1)
    public void visor$renderVRGuiPhase(GameRenderer instance, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance);
            return;
        }
        // keeps visor$matrix's identity() off the model-view base
        McModelViewStack.push();
        try {
            original.call(instance);
        } finally {
            McModelViewStack.pop();
            McModelViewStack.apply();
        }
    }
    //?} else {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"), method = "renderFrame", require = 1)
    public void visor$renderVRGuiPhase(GameRenderer instance, DeltaTracker deltaTracker, boolean renderLevel, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, deltaTracker, visor$levelInVanillaFrame);
            return;
        }
        // keeps visor$matrix's identity() off the model-view base
        McModelViewStack.push();
        try {
            original.call(instance, deltaTracker, visor$levelInVanillaFrame);
        } finally {
            McModelViewStack.pop();
            McModelViewStack.apply();
        }
    }
    *///?}

    /**
     * Calls VR rendering after mc rendered
     */
    // must sit between GameRenderer.render and blitToScreen, or the mirror is drawn after the window swap
    //? if >=26.3 {
    @Inject(at = @At(value = "CONSTANT", args = "stringValue=swapchainBlit"), method = "renderFrame", require = 1)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci) {
        visor$renderVRFrame(renderLevel, Util.getNanos());
    }
    //?} else {
    /*@Inject(at = @At(value = "CONSTANT", args = "stringValue=present"), method = "renderFrame", require = 1)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci) {
        visor$renderVRFrame(renderLevel, Util.getNanos());
    }
    *///?}

    @Unique
    private void visor$renderVRFrame(boolean renderLevel, long nanoTime) {
        if (ClientContext.visor != null) {
            ClientContext.visor
                    .renderVR(
                            new RenderContext(
                                    McVersionClientUtils.profiler(),
                                    renderLevel,
                                    nanoTime,
                                    visor$getPartialTicks()
                            )
                    );
        }
    }

    /**
     * Ensures the render phase
     * and main render target are correct on resize
     *
     * @param ci
     */
    @Inject(at = @At("HEAD"), method = "resizeGui")
    private void visor$ensurePhaseOnResize(CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            if (VisorState.get().isActive()) {
                VRRenderState.startVRGuiPhase();
            } else {
                VRRenderState.startVanillaPhase();
            }
        }
    }

    /**
     * Disables vanilla hit result calculation on tick.
     *
     * @param instance s
     * @param f        s
     */
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pick(F)V"), method = "tick")
    public void visor$noVanillaHitResult(Minecraft instance, float f, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, f);
        }
    }

    @WrapMethod(method = "pick(F)V")
    private void visor$pickWithVRHands(float partialTick, Operation<Void> original) {
        VRAimPicker.pickWithVRHands(() -> original.call(partialTick));
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pick(F)V"), method = "renderFrame", require = 1)
    private void visor$pickOncePerFrame(Minecraft instance, float partialTick, Operation<Void> original,
                                        @Local(argsOnly = true) boolean renderLevel) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, partialTick);
            return;
        }
        if (renderLevel && MC.level != null && MC.player != null && MC.isGameLoadFinished()) {
            original.call(instance, partialTick);

            if (McVersionClientUtils.screen() == null) {
                TaskTeleport.updateTeleportDestination(MC.player);
            }
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isFocused()Z"), method = "pauseIfInactive", require = 1)
    private boolean visor$noPauseGameIfWindowNotFocused(Window window, Operation<Boolean> original) {
        return VisorState.get().isActive() || original.call(window);
    }

    //? if >=26.2 {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuSurface$PresentMode;getSupportedVsyncMode(Ljava/util/Collection;Z)Lcom/mojang/renderpearl/api/device/GpuSurface$PresentMode;", remap = false), method = "renderFrame", index = 1, require = 1)
    private boolean visor$noVsync(boolean vsync) {
        return vsync && VisorState.get().isNotActive();
    }
    //?}


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    public float visor$getPartialTicks() {
        return McRenderUtils.partialTick();
    }
}

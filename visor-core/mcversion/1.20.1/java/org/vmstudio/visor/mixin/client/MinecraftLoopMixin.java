// #!MC-VERSION:: 1.20.1-1.21.11
package org.vmstudio.visor.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
//? if >=1.21.5 {
/*import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
*///?}
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.context.PreRenderContext;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.extensions.client.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.Util;
//? if >=1.21 {
/*import net.minecraft.client.DeltaTracker;
*///?}
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

    // ---- Shadow fields ----


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
    }

    @Unique
    private void visor$startGameLoop() {
        VisorState.updateState();
        //? if >=1.21.5 {
        /*if (VisorState.get().isInitialized()) {
            McRenderTarget.setMainTarget(VRRenderState.getVanillaTarget());
        }
        *///?}
        if(ClientContext.visor != null) {
            ClientContext.visor
                    .onGameLoopStart();
        }
    }

    @Inject(method = "runTick", at = @At(value = "CONSTANT", args = "stringValue=render"), require = 1)
    public void visor$preRenderVR(boolean tick, CallbackInfo callback) {
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

    //? if >=1.21 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"), method = "runTick", require = 1)
    public void visor$startVRGuiPhase(GameRenderer instance, DeltaTracker deltaTracker, boolean renderLevel, Operation<Void> original) {
        visor$renderVRGuiPhase(renderLevel, level -> original.call(instance, deltaTracker, level));
    }
    *///?} else {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), method = "runTick", require = 1)
    public void visor$startVRGuiPhase(GameRenderer instance, float partialTicks, long nanoTime, boolean renderLevel, Operation<Void> original) {
        visor$renderVRGuiPhase(renderLevel, level -> original.call(instance, partialTicks, nanoTime, level));
    }
    //?}

    private void visor$renderVRGuiPhase(boolean renderLevel, Consumer<Boolean> render) {
        if (VisorState.get().isNotActive()) {
            render.accept(renderLevel);
            return;
        }
        ClientContext.renderer.onGameRenderStart(renderLevel);
        boolean level = renderLevel && !VRRenderState.getPhase().isVRGui(); //disabled in VRGui phase, fallback on exception

        // keeps visor$matrix's identity() off the model-view base
        McModelViewStack.push();
        try {
            render.accept(level);
        } finally {
            McModelViewStack.pop();
            McModelViewStack.apply();
        }
    }

    /**
     * Calls VR rendering after mc rendered
     */
    // must sit between GameRenderer.render and blitToScreen, or the mirror is drawn after the window swap;
    // 1.21.9 turned the pop after GameRenderer.render into popPush("blit")
    //? if >=1.21.9 {
    /*@Inject(at = @At(value = "CONSTANT", args = "stringValue=blit"), method = "runTick", require = 1)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci) {
        visor$renderVRFrame(renderLevel, Util.getNanos());
    }
    *///?} elif >=1.21.2 {
    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 3, shift = Shift.AFTER), method = "runTick", require = 1)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci, @Local(ordinal = 0) long nanoTime) {
        visor$renderVRFrame(renderLevel, nanoTime);
    }
    *///?} else {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 4, shift = Shift.AFTER), method = "runTick", require = 1)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci, @Local(ordinal = 0) long nanoTime) {
        visor$renderVRFrame(renderLevel, nanoTime);
    }
    //?}

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
    @Inject(at = @At("HEAD"), method = "resizeDisplay")
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
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick")
    public void visor$noVanillaHitResult(GameRenderer instance, float f, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(instance, f);
        }
    }

    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    public float visor$getPartialTicks() {
        return McRenderUtils.partialTick();
    }
}

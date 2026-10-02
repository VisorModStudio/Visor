package org.vmstudio.visor.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.MainTarget;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.tasks.types.movement.vehicle.TaskVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftLifecycleMixin {

    // ---- Shadow fields ----
    @Shadow
    public LocalPlayer player;

    // ---- Shadow methods ----
    @Shadow
    public abstract Entity getCameraEntity();


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    /**
     * Instantiates RenderStageManager with
     * a vanilla main render target.
     * <br>
     * We need it early created
     * and separately from Visor initialization
     *
     * @param overlay s
     * @return s
     */
    //? if >=26.2 {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0, require = 1)
    //?} else {
    /*@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0, require = 1)
    *///?}
    public Overlay visor$initRenderStageManager(Overlay overlay) {
        VRRenderState.initVanillaTarget((MainTarget) McRenderTarget.mainTarget());

        return overlay;
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"), require = 1)
    public void visor$onGameLoadFinish(CallbackInfo ci) {
        VisorState.setMinecraftLoaded(true);

    }

    /**
     * Disables Thread.sleep()
     * call in vanilla when waiting for world to finish loading.
     * <p>
     * FPS has to be handled only by VR related features
     */
    // 1.21.9 dropped the sleep from doWorldLoad, nothing left to cancel
    //? if <1.21.9 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"), method = "doWorldLoad", expect = 0)
    private void visor$cancelFPSLimitOnWorldLoad(long l, Operation<Void> original) {
        if (VisorState.get().isActive()) {
            return;
        }
        original.call(l);
    }
    *///?}


    /**
     * Release data that won't be updating
     * during world load (like input and mb something else)
     */
    @Inject(method = "doWorldLoad", at = @At("HEAD"))
    private void visor$onWorldLoad(CallbackInfo ci) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        try {
            var activeSet = ClientContext.inputManager.getActiveSet();
            if (activeSet != null) {
                activeSet.clear();
            }
        } catch (Throwable ignored) {
            // Don't block world load
        }
    }

    /**
     * Resets room origin when world changed
     *
     * @param info s
     */
    @Inject(at = @At("HEAD"), method = "setLevel")
    public void visor$onLevelChange(CallbackInfo info) {
        if (VisorState.get().isActive()) {
            ClientContext.localPlayer.setOrigin(
                    0.0f, 0.0f, 0.0f, true
            );
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void visor$markVrShutdown(CallbackInfo ci) {
        try {
            if (ClientContext.visor == null) {
                return;
            }
            ClientContext.visor.getVrProvider().prepareDestroy();
        } catch (Throwable ignored) {
            // Don't block
        }
    }
    // close() overrides AutoCloseable, so it carries the same name in every namespace
    @Inject(method = "close", at = @At("HEAD"), remap = false)
    private void visor$destroyVrOnClose(CallbackInfo ci) {
        try {
            if (VisorState.get().isInitialized()) {
                VisorState.destroyVR();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    @Inject(method = "setCameraEntity", at = @At("HEAD"), cancellable = true)
    private void visor$rideEntity(Entity entity, CallbackInfo ci) {
        var state = VisorState.get();
        if (!state.isInitialized() || entity == null) {
            return;
        }

        if (state.isActive()
                && this.player != null
                && this.player.isSpectator()
                && entity != this.player) {
            ci.cancel(); // cancel spectate entity in VR
            return;
        }

        if (entity != this.getCameraEntity()) {
            ClientContext.localPlayer.recenterOrigin(entity, true);
        }
        if (entity != this.player) {
            TaskVehicle.getInstance().onStartRiding(entity);
        } else {
            TaskVehicle.getInstance().onStopRiding();
        }
    }
}

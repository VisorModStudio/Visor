package me.phoenixra.visor.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;

import me.phoenixra.visor.core.client.render.context.PreRenderContext;
import me.phoenixra.visor.core.client.render.context.RenderContext;
import me.phoenixra.visor.api.client.input.HandAction;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import me.phoenixra.visor.core.client.tasks.types.movement.vehicle.TasVehicle;
import me.phoenixra.visor.modified.client.MinecraftModified;
import me.phoenixra.visor.modified.client.entity.LocalPlayerModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import me.phoenixra.visor.core.client.VisorState;

import me.phoenixra.visor.core.client.ClientContext;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftModified {


    @Final
    @Shadow
    public Gui gui;


    @Shadow
    public Screen screen;

    @Shadow
    private ProfilerFiller profiler;


    @Final
    @Shadow
    public static boolean ON_OSX;

    @Shadow
    private boolean pause;

    @Shadow
    private float pausePartialTick;

    @Final
    @Shadow
    private Timer timer;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    public ClientLevel level;

    @Shadow
    public RenderTarget mainRenderTarget;

    @Shadow
    public LocalPlayer player;
    @Shadow
    public abstract Entity getCameraEntity();

    @Shadow
    public abstract void tick();




     /* *************************** *\
   //--------VR INITIALIZATION--------\\
     \* *************************** */

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
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0)
    public Overlay visor$initRenderStageManager(Overlay overlay) {
        VRRenderState.initVanillaTarget((MainTarget) this.mainRenderTarget);

        return overlay;
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    public void visor$onGameLoadFinish(CallbackInfo ci){
        VisorState.setMinecraftLoaded(true);

    }



     /* ***************** *\
   //--------TICKING--------\\
     \* ***************** */

    /**
     * Pre Ticks Visor right before mc tick() is called
     * @param ci s
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V"), method = "runTick")
    public void visor$preTick(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ClientContext.visor.preTickVR();
        }
    }

    /**
     * Ticks Visor (before mc tick methods called)
     * @param info s
     */
    @Inject(at = @At("HEAD"), method = "tick()V")
    public void visor$tick(CallbackInfo info) {
        if (VisorState.get().isActive()) {
            ClientContext.visor.tickVR();
        }
    }

    /**
     * Post Ticks Visor right after mc tick() is called
     * @param ci s
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER), method = "runTick")
    public void visor$postTick(CallbackInfo ci) {
        if (VisorState.get().isActive()) {
            ClientContext.visor.postTickVR();
        }
    }



     /* ******************* *\
   //--------RENDERING--------\\
     \* ******************* */

    /**
     * Calls pre render task at the beginning of a frame
     * @param tick s
     * @param callback s
     */
     @Inject(at = @At("HEAD"), method = "runTick(Z)V")
     public void visor$runVR(boolean tick, CallbackInfo callback) {
         VisorState.updateState();
         if (VisorState.get().isActive()) {
             ++VisorState.FRAME_COUNT;

             ClientContext.visor
                     .onGameLoopStart();

         }
     }

    @Inject(method = "runTick", at = @At(value = "CONSTANT", args = "stringValue=render"))
    public void visor$preRenderVR(boolean tick, CallbackInfo callback) {
        if (VisorState.get().isActive()) {

            ClientContext.visor
                    .preRenderVR(new PreRenderContext(
                            profiler, tick,
                            visor$getPartialTicks()
                    ));

        }
    }

    /**
     * Modifies vanilla GameRenderer.render() call
     * to update renderer state and start VRGui phase instead
     * @param renderLevel s
     * @return s
     */
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), method = "runTick")
    public boolean visor$startVRGuiPhase(boolean renderLevel) {
        if (VisorState.get().isActive()) {

            ClientContext.renderer.onGameRenderStart(renderLevel);

            if (VRRenderState.getPhase().isVRGui()) {
                return false; //disable level rendering
            } else {
                return renderLevel; //fallback on exception
            }
        }
        return renderLevel;
    }

    /**
     * Calls VR rendering after mc rendered
     *
     * @param renderLevel s
     * @param ci s
     * @param nanoTime s
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 4, shift = Shift.AFTER), method = "runTick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void visor$renderVR(boolean renderLevel, CallbackInfo ci, long nanoTime) {
        if (VisorState.get().isActive()) {
            ClientContext.visor
                    .renderVR(new RenderContext(
                            profiler,
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
    void visor$ensurePhaseOnResize(CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            if (VisorState.get().isActive()) {
                VRRenderState.startVRGuiPhase();
            } else {
                VRRenderState.startVanillaPhase();
            }
        }
    }

    /**
     * Disables Thread.sleep()
     * call in vanilla when waiting for world to finish loading.
     * <p>
     * FPS has to be handled only by VR related features
     *
     */
    @WrapOperation(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"), method = "doWorldLoad", expect = 0)
    private void visor$noFPSLimitOnWorldLoad(long l, Operation<Void> original) {
        if (VisorState.get().isActive()) {
            return;
        }
        original.call(l);
    }


     /* ******************* *\
   //--------VR OVERLAYS--------\\
     \* ******************* */

    /**
     * Handles screen changes
     *
     * @param pGuiScreen s
     * @param info s
     */
    @Inject(at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void visor$onOpenScreen(Screen pGuiScreen, CallbackInfo info) {
        if(VisorState.get().isNotActive()) return;

        ClientContext.overlayManager
                .getOverlay(VROverlayGameScreen.ID, VROverlayGameScreen.class)
                .onScreenChanged(this.screen, pGuiScreen, true);
    }

    /**
     * Handles overlay changes
     *
     * @param overlay s
     * @param ci s
     */
    @Inject(at = @At("TAIL"), method = "setOverlay")
    public void visor$onOverlaySet(Overlay overlay, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;

        ClientContext.overlayManager
                .getOverlay(VROverlayGameScreen.ID, VROverlayGameScreen.class)
                .onScreenChanged(this.screen, this.screen, true);
    }

    /**
     * Ticks VR overlays right after mc ticked screen
     * @param ci s
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;tick(Z)V"))
    private void visor$tickVrOverlays(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;

        if (ClientContext.overlayManager == null) return;
        ClientContext.overlayManager.tick();
    }

      /* *************** *\
    //--------INPUT--------\\
      \* *************** */

    /**
     * Overrides an action performed when
     * pressed "keyTogglePerspective" button
     * <br>
     * So, instead this button changes mirror camera type
     *
     * @param instance   s
     * @param cameraType s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
    public void visor$toggleMirrorButton(Options instance, CameraType cameraType) {
        if (VisorState.get().isActive()) {
            ClientContext.settingsManager.nextOptionValue(
                    VROptionWidgetType.MIRROR_MODE.getKey()
            );
        } else {
            instance.setCameraType(cameraType);
        }
    }

    /**
     * Disables last method that can be called when
     * pressed "keyTogglePerspective" button
     *
     * @param instance s
     * @param entity   s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
    public void visor$noTogglePerspectiveAction(GameRenderer instance, Entity entity) {
        if (VisorState.get().isNotActive()) {
            instance.checkEntityPostEffect(entity);
        }
    }


    /**
     * Uses VR method for hand swinging on item drop
     * instead of vanilla
     *
     * @param instance        s
     * @param interactionHand s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
    public void visor$swingDrop(LocalPlayer instance, InteractionHand interactionHand) {
        if (VisorState.get().isActive()) {
            ((LocalPlayerModified) player).visor$swingArm(
                    interactionHand, HandAction.ATTACK
            );
        } else {
            instance.swing(interactionHand);
        }
    }


     /* ************************************* *\
   //--------IF OFFHAND SUPPORT DISABLED--------\\
     \* ************************************* */


    /**
     * Replaces vanilla swing with VR swing
     * (USE)
     * @param instance s
     * @param interactionHand s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
    public void visor$swingUse(LocalPlayer instance, InteractionHand interactionHand) {
        if (VisorState.get().isNotActive()) {
            instance.swing(interactionHand);
            return;
        }
        ((LocalPlayerModified) instance).visor$swingArm(
                interactionHand, HandAction.USE
        );
    }


     /* ****************** *\
   //--------VR MOUSE--------\\
     \* ****************** */

    /**
     * Makes mouse always grabbed,
     * since it should not be disabled in VR mode
     *
     * @param instance s
     * @return s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), method = "handleKeybinds")
    public boolean visor$mouseAlwaysGrabbed(MouseHandler instance) {
        return VisorState.get().isActive() || instance.isMouseGrabbed();
    }





     /* **************** *\
   //--------EVENTS--------\\
     \* **************** */

    /**
     * Resets room origin when world changed
     *
     * @param pLevelClient s
     * @param info s
     */
    @Inject(at = @At("HEAD"), method = "setLevel")
    public void visor$onLevelChange(ClientLevel pLevelClient, CallbackInfo info) {
        if (VisorState.get().isActive()) {
            ClientContext.localPlayer.setOrigin(
                    0.0f, 0.0f, 0.0f, true
            );
        }
    }


     /* ************** *\
   //--------MISC--------\\
     \* ************** */
     @Inject(method = "setCameraEntity", at = @At("HEAD"))
     private void visor$rideEntity(Entity entity, CallbackInfo ci) {
         if (VisorState.get().isInitialized() && entity != null) {
             if (entity != this.getCameraEntity()) {
                 // snap to entity, if it changed
                 ClientContext.localPlayer.recenterOrigin(entity, true);
             }
             if (entity != this.player) {
                 // ride the new camera entity
                 TasVehicle.getInstance().onStartRiding(entity);
             } else {
                 TasVehicle.getInstance().onStopRiding();
             }
         }
     }
    /**
     * Disables vanilla hit result calculation on tick.
     *
     * @param instance s
     * @param f        s
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick")
    public void visor$noVanillaHitResult(GameRenderer instance, float f) {
        if (VisorState.get().isNotActive()) {
            instance.pick(f);
        }
    }


     /* ************************ *\
   //--------PUBLIC METHODS--------\\
     \* ************************ */



    @Override
    public float visor$getPartialTicks() {
        return pause ? pausePartialTick : this.timer.partialTick;
    }
}

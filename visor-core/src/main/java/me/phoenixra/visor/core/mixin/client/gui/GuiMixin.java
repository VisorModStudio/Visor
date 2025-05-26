package me.phoenixra.visor.core.mixin.client.gui;

import me.phoenixra.visor.core.client.mcmodified.GuiModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Gui.class)
public abstract class GuiMixin implements GuiModified {

    @Final
    @Shadow
    private Minecraft minecraft;

    /* ********************************** *\
  //--------DISABLE VANILLA OVERLAYS--------\\
    \* ********************************** */

    @Shadow public abstract void render(GuiGraphics guiGraphics, float f);

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void visor$render(GuiGraphics guiGraphics, float f, CallbackInfo ci){

        if(VRRenderState.getCurrentPhase().isVRGui()
                && minecraft.screen != null) {
            ci.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    public void visor$noVanillaHotbar(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()
                && VRClientSettings.isHudDisableHotBar()) {
            ci.cancel();
        }
    }



    @Inject(at = @At("HEAD"), method = "renderVignette", cancellable = true)
    public void visor$noVanillaVignette(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }

    }
    @Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    public void visor$noVanillaSpyglassOverlay(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
    public void visor$noVanillaEffects(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "renderSelectedItemName", cancellable = true)
    public void visor$noVanillaSelectedItemName(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }
    @Inject(at = @At("HEAD"), method = "renderSavingIndicator", cancellable = true)
    public void visor$noAutoSaveText(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void visor$noTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void visor$noPortalOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    public void visor$noCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "render")
    public int visor$noSleepOverlay(LocalPlayer instance) {
        if(VRRenderState.getCurrentPhase().isVRGui()){
            return 0;
        }
        return instance.getSleepTimer();
    }

}

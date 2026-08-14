package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.GuiExtension;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
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
public abstract class GuiMixin implements GuiExtension {

    @Final
    @Shadow
    private Minecraft minecraft;

    /* ********************************** *\
  //--------DISABLE VANILLA OVERLAYS--------\\
    \* ********************************** */
    // 1.21.1: renderHotbar split into renderHotbarAndDecorations/renderItemHotbar
    @Inject(at = @At("HEAD"), method = "renderItemHotbar", cancellable = true)
    public void visor$noVanillaHotbar(CallbackInfo ci) {
        if(VisorState.get().isNotActive()
                || (minecraft.screen == null
                && !VRClientSettings.isHudDisableHotBar()
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderPlayerHealth", cancellable = true)
    public void visor$noVanillaPlayerHealth(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderVehicleHealth", cancellable = true)
    public void visor$noVanillaVehicleHealth(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderJumpMeter", cancellable = true)
    public void visor$noVanillaJumpMeter(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderExperienceBar", cancellable = true)
    public void visor$noVanillaExperienceBar(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    // 1.21.1: the level number is drawn by its own layer now
    @Inject(at = @At("HEAD"), method = "renderExperienceLevel", cancellable = true)
    public void visor$noVanillaExperienceLevel(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    // 1.21.1: the boss bar call lives in a constructor lambda now,
    // so it is cancelled in BossHealthOverlayMixin instead of redirected here
    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V"),
            method = "renderChat")
    public void visor$noVanillaGuiChat(ChatComponent instance,
                                       GuiGraphics guiGraphics,
                                       int i, int j, int k, boolean focused) {
        if(VisorState.get().isNotActive()) {
            instance.render(guiGraphics, i, j, k, focused);
            return;
        }
        if(minecraft.screen instanceof ChatScreen) {
            instance.render(guiGraphics, i, j, k, focused);
        }
    }


    @Inject(at = @At("HEAD"), method = "renderVignette", cancellable = true)
    public void visor$noVanillaVignette(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    public void visor$noVanillaSpyglassOverlay(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
    public void visor$noVanillaEffects(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderSelectedItemName", cancellable = true)
    public void visor$noVanillaSelectedItemName(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderSavingIndicator", cancellable = true)
    public void visor$noAutoSaveText(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void visor$noTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void visor$noPortalOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    public void visor$noCrosshair(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    // 1.21.1: sleep overlay drawn by its own layer method
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "renderSleepOverlay")
    public int visor$noSleepOverlay(LocalPlayer instance) {
        return VisorState.get().isActive()
                ? 0
                : instance.getSleepTimer();
    }

}

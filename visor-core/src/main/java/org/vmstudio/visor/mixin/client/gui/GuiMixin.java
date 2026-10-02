package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.GuiExtension;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
//? if >=1.21.2 {
import org.vmstudio.visor.core.client.render.VRRenderState;
//?}
//? if >=1.21.6 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Unique;
//?}


//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class GuiMixin implements GuiExtension {

    @Final
    @Shadow
    private Minecraft minecraft;

    /* ********************************** *\
  //--------DISABLE VANILLA OVERLAYS--------\\
    \* ********************************** */
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractItemHotbar", cancellable = true)
    //?} elif >=1.20.5 {
    /*@Inject(at = @At("HEAD"), method = "renderItemHotbar", cancellable = true)
    *///?} else {
    /*@Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    *///?}
    public void visor$noVanillaHotbar(CallbackInfo ci) {
        if(VisorState.get().isNotActive()
                || (McVersionClientUtils.screen() == null
                && !VRClientSettings.isHudDisableHotBar()
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractPlayerHealth", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderPlayerHealth", cancellable = true)
    *///?}
    public void visor$noVanillaPlayerHealth(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractVehicleHealth", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderVehicleHealth", cancellable = true)
    *///?}
    public void visor$noVanillaVehicleHealth(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            method = {"extractHotbarAndDecorations", "extractContextualInfoBarBackground"})
    private void visor$noVanillaContextualBarBackground(ContextualBar instance, GuiGraphicsExtractor guiGraphics,
                                                        DeltaTracker deltaTracker, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(instance, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            method = {"extractHotbarAndDecorations", "extractContextualInfoBar"})
    private void visor$noVanillaContextualBar(ContextualBar instance, GuiGraphicsExtractor guiGraphics,
                                              DeltaTracker deltaTracker, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(instance, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"),
            method = {"extractHotbarAndDecorations", "extractExperienceLevel"})
    private void visor$noVanillaExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(guiGraphics, font, level);
        }
    }
    //?} elif >=1.21.6 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            method = {"renderHotbarAndDecorations", "renderContextualInfoBarBackground"})
    private void visor$noVanillaContextualBarBackground(ContextualBar instance, GuiGraphicsExtractor guiGraphics,
                                                        DeltaTracker deltaTracker, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(instance, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"),
            method = {"renderHotbarAndDecorations", "renderContextualInfoBar"})
    private void visor$noVanillaContextualBar(ContextualBar instance, GuiGraphicsExtractor guiGraphics,
                                              DeltaTracker deltaTracker, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(instance, guiGraphics, deltaTracker);
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"),
            method = {"renderHotbarAndDecorations", "renderExperienceLevel"})
    private void visor$noVanillaExperienceLevel(GuiGraphicsExtractor guiGraphics, Font font, int level, Operation<Void> original) {
        if (visor$hudBarsVisible()) {
            original.call(guiGraphics, font, level);
        }
    }
    *///?} else {
    /*@Inject(at = @At("HEAD"), method = "renderJumpMeter", cancellable = true)
    public void visor$noVanillaJumpMeter(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    @Inject(at = @At("HEAD"), method = "renderExperienceBar", cancellable = true)
    public void visor$noVanillaExperienceBar(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    *///?}
    //? if >=1.21.6 {
    @Unique
    private boolean visor$hudBarsVisible() {
        return VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD));
    }
    //?}
    //? if >=1.20.5 && <1.21.6 {
    /*@Inject(at = @At("HEAD"), method = "renderExperienceLevel", cancellable = true)
    public void visor$noVanillaExperienceLevel(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (McVersionClientUtils.screen() == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
    *///?}
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractChat", cancellable = true)
    public void visor$noVanillaGuiChat(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    //?} elif >=1.20.5 {
    /*@Inject(at = @At("HEAD"), method = "renderChat", cancellable = true)
    public void visor$noVanillaGuiChat(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    *///?} else {
    /*@WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphicsExtractor;III)V"),
            method = "render")
    public void visor$noVanillaGuiChat(ChatComponent instance,
                                       GuiGraphicsExtractor guiGraphics,
                                       int i, int j, int k, Operation<Void> original) {
        if(VisorState.get().isNotActive()) {
            original.call(instance, guiGraphics, i, j, k);
            return;
        }
        if(McVersionClientUtils.screen() instanceof ChatScreen) {
            original.call(instance, guiGraphics, i, j, k);
        }
    }
    *///?}


    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractVignette", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderVignette", cancellable = true)
    *///?}
    public void visor$noVanillaVignette(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractSpyglassOverlay", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    *///?}
    public void visor$noVanillaSpyglassOverlay(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractEffects", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
    *///?}
    public void visor$noVanillaEffects(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractSelectedItemName", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderSelectedItemName", cancellable = true)
    *///?}
    public void visor$noVanillaSelectedItemName(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }
    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractSavingIndicator", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderSavingIndicator", cancellable = true)
    *///?}
    public void visor$noAutoSaveText(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    //? if >=26.1 {
    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    //?} else {
    /*@Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    *///?}
    public void visor$noTextureOverlay(GuiGraphicsExtractor guiGraphics, Identifier resourceLocation, float f, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    //? if >=26.1 {
    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    //?} else {
    /*@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    *///?}
    public void visor$noPortalOverlay(GuiGraphicsExtractor guiGraphics, float f, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    //? if >=26.1 {
    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void visor$noConfusionOverlayInGUI(GuiGraphicsExtractor guiGraphics, float f, CallbackInfo ci) {
        if (VRRenderState.getPhase().isVRGui()) {
            ci.cancel();
        }
    }
    //?} elif >=1.21.2 {
    /*@Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void visor$noConfusionOverlayInGUI(GuiGraphicsExtractor guiGraphics, float f, CallbackInfo ci) {
        if (VRRenderState.getPhase().isVRGui()) {
            ci.cancel();
        }
    }
    *///?}

    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractCrosshair", cancellable = true)
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    *///?}
    public void visor$noCrosshair(CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;
        ci.cancel();
    }

    //? if >=26.1 {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "extractSleepOverlay")
    //?} elif >=1.20.5 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "renderSleepOverlay")
    *///?} else {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "render")
    *///?}
    public int visor$suppressSleepFade(LocalPlayer instance, Operation<Integer> original) {
        return VisorState.get().isActive()
                ? 0
                : original.call(instance);
    }

}

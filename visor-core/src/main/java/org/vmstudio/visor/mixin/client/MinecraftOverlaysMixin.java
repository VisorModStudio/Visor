package org.vmstudio.visor.mixin.client;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
//? if >=26.2 {
import net.minecraft.client.gui.Gui;
//?} else {
/*import net.minecraft.client.Minecraft;
*///?}
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Gui.class)
//?} else {
/*@Mixin(Minecraft.class)
*///?}
public abstract class MinecraftOverlaysMixin {

    // ---- Shadow fields ----
    //? if >=26.2 {
    @Shadow
    private Screen screen;
    //?} else {
    /*@Shadow
    public Screen screen;
    *///?}


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    /**
     * Handles screen changes
     *
     * @param pGuiScreen s
     * @param info       s
     */
    //? if >=26.2 {
    @Inject(at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/Gui;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", require = 1)
    //?} else {
    /*@Inject(at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", require = 1)
    *///?}
    public void visor$onOpenScreen(Screen pGuiScreen, CallbackInfo info) {
        if (VisorState.get().isNotActive()) return;

        ClientContext.overlayManager
                .getOverlay(VROverlayGameScreen.ID, VROverlayGameScreen.class)
                .onScreenChanged(this.screen, pGuiScreen, true);
    }

    /**
     * Handles overlay changes
     *
     * @param overlay s
     * @param ci      s
     */
    @Inject(at = @At("TAIL"), method = "setOverlay")
    public void visor$onOverlaySet(Overlay overlay, CallbackInfo ci) {
        if (VisorState.get().isNotActive()) return;

        ClientContext.overlayManager
                .getOverlay(VROverlayGameScreen.ID, VROverlayGameScreen.class)
                .onScreenChanged(this.screen, this.screen, true);
    }

    /**
     * Ticks VR overlays right after mc ticked screen
     *
     * @param ci s
     */
    //? if >=26.2 {
    @Inject(method = "tick", at = @At("HEAD"))
    //?} else {
    /*@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;tick(Z)V"))
    *///?}
    private void visor$tickVrOverlays(CallbackInfo ci) {
        if (VisorState.get().isNotActive()) return;

        if (ClientContext.overlayManager == null) return;
        ClientContext.overlayManager.tick();
    }
}

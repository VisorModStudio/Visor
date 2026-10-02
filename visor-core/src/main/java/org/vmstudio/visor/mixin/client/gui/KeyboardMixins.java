package org.vmstudio.visor.mixin.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
//? if >=1.21.9 {
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;



public class KeyboardMixins {


    @Mixin(InputConstants.class)
    public static class InputConstantsMixin {

        /**
         * For keyboard to work
         */
        //? if >=1.21.9 {
        @Inject(at = @At("HEAD"), method = "isKeyDown", cancellable = true)
        private static void visor$keyDown(Window window, int i, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(
                    GLFW.glfwGetKey(window.handle(), i) == 1
                            || (VisorState.get().isActive() && InputHelper.isKeyDown(i))
            );
        }
        //?} else {
        /*@Inject(at = @At("HEAD"), method = "isKeyDown", cancellable = true)
        private static void visor$keyDown(long l, int i, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(
                    GLFW.glfwGetKey(l, i) == 1
                            || (VisorState.get().isActive() && InputHelper.isKeyDown(i))
            );
        }
        *///?}
    }

    @Mixin(KeyboardHandler.class)
    public static class KeyboardHandlerMixin {

        @Final
        @Shadow
        private Minecraft minecraft;

        /**
         * Send keyboard events to overlay
         * if keyboard is attached to an overlay
         */
        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", ordinal = 0, shift = At.Shift.AFTER), method = "keyPress", cancellable = true)
        //? if >=1.21.9 {
        public void visor$onKeyPressed(long windowHandle, int actionType, KeyEvent event, CallbackInfo ci) {
            int keyCode = event.key();
            int keyScan = event.scancode();
            int keyModifiers = event.modifiers();
        //?} else {
        /*public void visor$onKeyPressed(long windowHandle, int keyCode, int keyScan, int actionType, int keyModifiers, CallbackInfo ci) {
        *///?}
            if (VisorState.get().isNotActive()) {
                return;
            }
            var keyboardAccessor = ClientContext.overlayManager.getKeyboardAccessor();
            Screen attachedScreen = keyboardAccessor
                    .getAttachedTo();
            if(attachedScreen instanceof VROverlayScreen overlay
                    && keyboardAccessor.isVisible()){
                if (actionType == 0) {
                    overlay.keyReleased(keyCode, keyScan, keyModifiers);
                } else {
                    //pressed - 1, heldDown - 2
                    overlay.keyPressed(keyCode, keyScan, keyModifiers);
                }
                ci.cancel();
            }
        }
    }


    /* ************************** *\
  //--------OPENING KEYBOARD--------\\
    \* *************************** */

    @Mixin(EditBox.class)
    public abstract static class EditBoxMixin extends AbstractWidget {

        public EditBoxMixin(int i, int j,
                            int k, int l,
                            Component component
        ) {
            super(i, j, k, l, component);
        }

        //? if >=1.21.9 {
        @Inject(at = @At(value = "HEAD"), method = "onClick")
        public void visor$openKeyboard(MouseButtonEvent event, boolean doubleClick, CallbackInfo ci) {
        //?} else {
        /*@Inject(at = @At(value = "HEAD"), method = "onClick")
        public void visor$openKeyboard(double d, double e, CallbackInfo ci) {
        *///?}
            if (VisorState.get().isNotActive()) {
                return;
            }

            var keyboardAccessor = ClientContext.overlayManager
                    .getKeyboardAccessor();
            var cursorHandler = ClientContext.cursorHandler;
            if (cursorHandler.isCursorHandFocused()) {
                VROverlayScreen overlayBase = null;
                if (cursorHandler.getFocusedOverlay() instanceof VROverlayScreen overlayScreen) {
                    overlayBase = overlayScreen;
                }
                Screen screenFocused = overlayBase == null
                        ? McVersionClientUtils.screen()
                        : overlayBase;
                keyboardAccessor.showKeyboard(
                        screenFocused
                );
            }
        }
    }


    @Mixin(AbstractSignEditScreen.class)
    public abstract static class AbstractSignEditScreenMixin extends Screen {

        protected AbstractSignEditScreenMixin(Component component) {
            super(component);
        }

        @Inject(at = @At("HEAD"), method = "init")
        public void visor$onInit(CallbackInfo ci) {
            if (VisorState.get().isNotActive()) {
                return;
            }
            var keyboardAccessor = ClientContext.overlayManager
                    .getKeyboardAccessor();
            keyboardAccessor.showKeyboard(this);
        }

        @Inject(at = @At("HEAD"), method = "removed")
        public void visor$onRemoved(CallbackInfo ci) {
            if (VisorState.get().isNotActive()) {
                return;
            }
            var keyboardAccessor = ClientContext.overlayManager
                    .getKeyboardAccessor();
            if(keyboardAccessor.isStaticAttachment()) {
                keyboardAccessor.showKeyboard(null);
            }else {
                keyboardAccessor.setVisible(false);
            }
        }
    }

    @Mixin(BookEditScreen.class)
    public abstract static class BookEditScreenMixin extends Screen {

        protected BookEditScreenMixin(Component component) {
            super(component);
        }

        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;updateButtonVisibility()V", shift = At.Shift.BEFORE), method = "init")
        public void visor$onInit(CallbackInfo ci) {
            if (VisorState.get().isNotActive()) {
                return;
            }
            var keyboardAccessor = ClientContext.overlayManager
                    .getKeyboardAccessor();
            keyboardAccessor.showKeyboard(this);

        }
    }

}

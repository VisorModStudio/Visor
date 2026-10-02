package org.vmstudio.visor.mixin.client.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.render.VRSceneType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
//?}

import java.io.File;
import java.util.function.Consumer;


@Mixin(KeyboardHandler.class)
public class KeybindingsMixin {

    //? if >=1.21.9 {
    @Inject(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), cancellable = true)
    private void visor$handleVRHotKeys(long windowPointer,
                                    int action, KeyEvent event,
                                    CallbackInfo ci) {
        int key = event.key();
        if (action == GLFW.GLFW_PRESS) {
    //?} else {
    /*@Inject(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), cancellable = true)
    private void visor$handleVRHotKeys(long windowPointer,
                                    int key, int scanCode,
                                    int action, int modifiers,
                                    CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
    *///?}
            if (InputHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                if (key == GLFW.GLFW_KEY_F7
                        && VisorAPI.clientState().sceneType() == VRSceneType.MAIN_MENU) {
                    VRPlayMode mode = VisorAPI.clientState().playMode().next();
                    VisorState.setVrPlayMode(mode);
                    ClientContext.settingsManager.saveOptions();
                    ci.cancel();
                }
            }
        }
    }
    //? if <26.2 {
    /*@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"), method = "keyPress")
    public void visor$screenshot(File file, RenderTarget renderTarget, Consumer<Component> consumer, Operation<Void> original) {
        if (VisorState.get().isNotActive()) {
            original.call(file, renderTarget, consumer);
            return;
        }
        ClientContext.renderer.setAskedForScreenShot(true);
    }
    *///?}
}

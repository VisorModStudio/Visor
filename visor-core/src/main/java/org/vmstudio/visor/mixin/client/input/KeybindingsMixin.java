package org.vmstudio.visor.mixin.client.input;

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
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.core.client.settings.VRClientSettings;


import java.io.File;
import java.util.function.Consumer;


@Mixin(KeyboardHandler.class)
public class KeybindingsMixin {

    @Inject(method = "keyPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0), cancellable = true)
    private void visor$handleVRHotKeys(long windowPointer,
                                    int key, int scanCode,
                                    int action, int modifiers,
                                    CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            if (InputHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                if (key == GLFW.GLFW_KEY_F7
                        && VisorAPI.clientState().sceneType() == VRSceneType.MAIN_MENU) {
                    VRPlayMode mode = VisorAPI.clientState().playMode().next();
                    VRClientSettings.setVrPlayMode(mode);
                    ClientContext.settingsManager.saveOptions();
                    ci.cancel();
                }
            }
        }
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"), method = "keyPress")
    public void visor$screenshot(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        if (VisorState.get().isNotActive()) {
            Screenshot.grab(file, renderTarget, consumer);
            return;
        }
        ClientContext.renderer.setAskedForScreenShot(true);
    }
}

package org.vmstudio.visor.mixin.client.input;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


import java.io.File;
import java.util.function.Consumer;


@Mixin(KeyboardHandler.class)
public class KeybindingsMixin {


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"), method = "keyPress")
    public void visor$screenshot(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        if (VisorState.get().isNotActive()) {
            Screenshot.grab(file, renderTarget, consumer);
            return;
        }
        ClientContext.renderer.setAskedForScreenShot(true);
    }
}

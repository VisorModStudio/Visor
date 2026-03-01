package org.vmstudio.visor.mixin.client.gui.screen;



import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow protected EditBox input;

    protected ChatScreenMixin(Component component) {
        super(component);
    }
    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"),cancellable = true)
    private void visor$clearInputOnClose(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if(VisorState.get().isNotActive()) return;
        input.setValue("");

        cir.setReturnValue(true);
    }
}

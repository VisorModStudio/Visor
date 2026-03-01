package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TutorialToast.class)
public abstract class TutorialToastMixin implements Toast {

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void visor$noToast(GuiGraphics guiGraphics,
                                       ToastComponent toastComponent,
                                       long l, CallbackInfoReturnable<Visibility> ci) {
        if(VisorState.get().isNotActive()) return;
        ci.setReturnValue(Visibility.HIDE);
    }

}

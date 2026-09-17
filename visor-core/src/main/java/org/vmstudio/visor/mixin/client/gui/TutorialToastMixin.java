// #!MC-VERSION:: 1.21.3+
package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TutorialToast.class)
public abstract class TutorialToastMixin implements Toast {

    @Inject(at = @At("HEAD"), method = "getWantedVisibility", cancellable = true)
    public void visor$noToast(CallbackInfoReturnable<Visibility> ci) {
        if(VisorState.get().isNotActive()) return;
        ci.setReturnValue(Visibility.HIDE);
    }

}

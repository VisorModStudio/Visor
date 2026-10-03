package org.vmstudio.visor.compatibility.create.core.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(targets = "com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen", remap = false)
@MixinGate(classes = "com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen")
@Pseudo
public abstract class ValueSettingsScreenMixin {

    @Shadow
    private int ticksOpen;

    @Shadow
    protected abstract void saveAndClose(double pMouseX, double pMouseY);

    @Inject(
            method = "mouseReleased",
            at = @At("HEAD"),
            cancellable = true
    )
    private void visor$onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.get().isActive()) {
            if (ticksOpen <= 2) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            method = "keyReleased",
            at = @At("HEAD"),
            cancellable = true
    )
    private void visor$onKeyReleased(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.get().isActive()) {
            if (ticksOpen <= 2) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void visor$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (VisorState.get().isActive()) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                saveAndClose(mouseX, mouseY);
                cir.setReturnValue(true);
            }
        }
    }
}

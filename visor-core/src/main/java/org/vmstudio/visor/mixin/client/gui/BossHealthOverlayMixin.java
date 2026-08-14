package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.1: Gui invokes the boss bar through a constructor lambda,
 * so it can't be redirected from GuiMixin anymore; the overlay is
 * cancelled at its own render instead (only vanilla caller is Gui).
 */
@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void visor$noVanillaGuiBossHealth(CallbackInfo ci) {
        if(VisorState.get().isNotActive() || (minecraft.screen == null
                && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) return;
        ci.cancel();
    }
}

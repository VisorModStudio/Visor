// #!MC-VERSION:: 26.1.2+
package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class BossBarMixins {

    /**
     * Hides the vanilla boss bar
     */
    @Mixin(BossHealthOverlay.class)
    public static class BossBarHideMixin {

        @Final
        @Shadow
        private Minecraft minecraft;

        @Inject(at = @At("HEAD"), method = "extractRenderState", cancellable = true)
        public void visor$noVanillaGuiBossHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
            if (VisorState.get().isNotActive() || (minecraft.screen == null
                    && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) {
                return;
            }
            ci.cancel();
        }
    }
}

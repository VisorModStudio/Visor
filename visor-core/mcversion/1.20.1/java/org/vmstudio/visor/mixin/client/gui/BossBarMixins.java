// #!MC-VERSION:: 1.20.1-1.20.4
package org.vmstudio.visor.mixin.client.gui;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.gui.Gui;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

public class BossBarMixins {

    /**
     * Hides the vanilla boss bar
     */
    @Mixin(Gui.class)
    public static class BossBarHideMixin {

        @Final
        @Shadow
        private Minecraft minecraft;

        @WrapOperation(at = @At(value = "INVOKE",
                target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;)V"),
                method = "render")
        public void visor$noVanillaGuiBossHealth(BossHealthOverlay instance,
                                                 GuiGraphics guiGraphics, Operation<Void> original) {
            if(VisorState.get().isNotActive() || (minecraft.screen == null
                    && ClientContext.visor.isFeatureDisabled(ClientFeature.GUI_DISABLE_HUD))) {
                original.call(instance, guiGraphics);
            }
        }
    }
}

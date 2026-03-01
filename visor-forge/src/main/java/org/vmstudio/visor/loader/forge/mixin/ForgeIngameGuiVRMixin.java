package org.vmstudio.visor.loader.forge.mixin;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeGui.class)
public abstract class ForgeIngameGuiVRMixin {


    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void noHudElements(NamedGuiOverlay overlay, GuiGraphics guiGraphics,
                               CallbackInfoReturnable<Boolean> info) {
        if(overlay == VanillaGuiOverlay.CHAT_PANEL.type()
                && (Minecraft.getInstance().screen instanceof ChatScreen)){
            return;
        }
        if(VisorState.get().isNotActive()){
            return;
        }
        if(Minecraft.getInstance().screen != null){
            info.setReturnValue(true);
            return;
        }
        if(ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_DISABLE_HUD)){
            if(overlay == VanillaGuiOverlay.HOTBAR.type()
                    && !VRClientSettings.isHudDisableHotBar()){
                return;
            }
            info.setReturnValue(true);
        }

    }

}

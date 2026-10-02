package org.vmstudio.visor.loader.forge.mixin;

import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
//? if >=1.20.5 {
import net.minecraft.client.gui.Gui;
//?} else {
/*import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;

//? if >=1.20.5 {
// 1.20.5 dropped the ForgeGui overlay system, vanilla GuiMixin covers the whole HUD
@Mixin(Gui.class)
public abstract class ForgeIngameGuiVRMixin {
}
//?} else {
/*@Mixin(ForgeGui.class)
public abstract class ForgeIngameGuiVRMixin {



    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void noHudElements(NamedGuiOverlay overlay, GuiGraphicsExtractor guiGraphics,
                               CallbackInfoReturnable<Boolean> info) {

        if (VisorState.get().isNotActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        if (overlay == VanillaGuiOverlay.SLEEP_FADE.type()) {
            info.setReturnValue(true);
            return;
        }

        if (overlay == VanillaGuiOverlay.CHAT_PANEL.type()) {
            if (!(McVersionClientUtils.screen() instanceof ChatScreen)) {
                info.setReturnValue(true);
            }
            return;
        }

        if (visor$isForgeHud(overlay)
                && (McVersionClientUtils.screen() != null
                || ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_DISABLE_HUD))) {
            info.setReturnValue(true);
        }

    }

    @Unique
    private static boolean visor$isForgeHud(NamedGuiOverlay overlay) {
        return overlay == VanillaGuiOverlay.PLAYER_HEALTH.type()
                || overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()
                || overlay == VanillaGuiOverlay.FOOD_LEVEL.type()
                || overlay == VanillaGuiOverlay.AIR_LEVEL.type()
                || overlay == VanillaGuiOverlay.MOUNT_HEALTH.type()
                || overlay == VanillaGuiOverlay.JUMP_BAR.type()
                || overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                || overlay == VanillaGuiOverlay.BOSS_EVENT_PROGRESS.type();
    }

}
*///?}

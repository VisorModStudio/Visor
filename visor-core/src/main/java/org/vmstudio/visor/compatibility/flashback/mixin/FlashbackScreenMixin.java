package org.vmstudio.visor.compatibility.flashback.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.core.client.VisorState;

@Pseudo
@ClassDependentMixin("com.moulberry.flashback.screen.select_replay.SelectReplayScreen")
@Mixin(targets = {
        "com.moulberry.flashback.screen.select_replay.SelectReplayScreen",
        "com.moulberry.flashback.screen.EditReplayScreen",
        "com.moulberry.flashback.screen.CombineReplayScreen"
}, remap = false)
public class FlashbackScreenMixin {
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void visor$stopIllegalFlashbackEntry(CallbackInfo ci) {
        if (!VisorState.get().isActive()) return;

        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setScreen(new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(null),
                    Component.literal("§cVR Mode not supported for Flashback"),
                    Component.literal("Replay viewing and editing is disabled in VR due to incompatibility issues.\n\n" +
                            "Recording from VR works fine, but please switch back to PC in the main menu to view or edit replays.")
            ));
        });
        System.err.println("[Visor] Blocked attempt to enter Flashback screen while VR is active");
    }
}
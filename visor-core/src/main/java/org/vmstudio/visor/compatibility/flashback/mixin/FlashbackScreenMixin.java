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
        if (!VisorState.get().isInitialized()) return;

        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setScreen(new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(null),
                    Component.literal("§cReplay editor is disabled in VR mode or WORLD_ONLY playMode"),
                    Component.literal("Replay editing is disabled in VR.\n" +
                            "Please switch back to PC (NonVR to use Replay editor")
            ));
        });
        System.err.println("[Visor] Blocked attempt to enter Replay editor screen while in VR or WORLD_ONLY playMode");
    }
}
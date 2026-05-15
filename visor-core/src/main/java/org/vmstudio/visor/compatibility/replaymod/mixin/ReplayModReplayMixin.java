package org.vmstudio.visor.compatibility.replaymod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.core.client.VisorState;

@Pseudo
@ClassDependentMixin("com.replaymod.replay.ReplayModReplay")
@Mixin(targets = "com.replaymod.replay.ReplayModReplay", remap = false)
public class ReplayModReplayMixin {
    @Inject(method = "startReplay(Lcom/replaymod/replaystudio/replay/ReplayFile;ZZ)Lcom/replaymod/replay/ReplayHandler;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void visor$stopIllegalReplayEntry(@Coerce Object file, boolean compat, boolean async, CallbackInfoReturnable<Object> cir) {
        if (!VisorState.get().isActive()) return;
        cir.setReturnValue(null);

        // todo: Component.translatable instead Component.literal
        Minecraft.getInstance().tell(() -> {
            Minecraft.getInstance().setScreen(new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(null),
                    Component.literal("§cVR Mode not supported for Replay Mod"),
                    Component.literal("Replay editing is disabled in VR due to critical incompatibility issues\n\n" +
                            "Please switch back to PC in the main menu to use Replay Mod.")
            ));
        });

        System.err.println("[Visor] Blocked attempt to enter Replay Mod screen while VR is active");
    }
}
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
        if (!VisorState.get().isInitialized()) return;
        cir.setReturnValue(null);

        // todo: Component.translatable instead Component.literal
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
package org.vmstudio.visor.compatibility.flashback.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;

@Pseudo
@MixinGate(classes = "com.moulberry.flashback.exporting.ExportJob", methods = "runClientTick")
@Mixin(targets = "com.moulberry.flashback.exporting.ExportJob", remap = false)
public class FlashbackExportMixin {
    @Inject(method = "runClientTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", remap = true), remap = false, require = 0)
    private void visor$preTickRemote(boolean frozen, CallbackInfo ci) {
        if (VisorState.get().isActive()) return;
        VRClientPlayers.preTickRemote();
    }

    @Inject(method = "runClientTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = At.Shift.AFTER, remap = true), remap = false, require = 0)
    private void visor$postTickRemote(boolean frozen, CallbackInfo ci) {
        if (VisorState.get().isActive()) return;
        VRClientPlayers.postTickRemote();
    }

    // 26.1+: static render(RenderTarget, DeltaTracker.Timer)
    @Inject(method = "render", at = @At("HEAD"), remap = false, require = 0)
    private static void visor$preRenderRemote(CallbackInfo ci) {
        if (VisorState.get().isActive()) return;
        VRClientPlayers.preRenderRemote(McRenderUtils.partialTick());
    }

    // 1.21 - 1.21.11: gameRenderer.render(deltaTracker, true)
    @Inject(method = "doExport", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V", remap = true), remap = false, require = 0)
    private void visor$preRenderRemoteInline(CallbackInfo ci) {
        if (VisorState.get().isActive()) return;
        VRClientPlayers.preRenderRemote(McRenderUtils.partialTick());
    }
}
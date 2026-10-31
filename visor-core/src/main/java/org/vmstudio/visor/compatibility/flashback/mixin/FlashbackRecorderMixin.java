package org.vmstudio.visor.compatibility.flashback.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.RecorderModHelper;
import org.vmstudio.visor.core.client.network.ClientNetworking;

import java.util.function.Consumer;

@Pseudo
@MixinGate(classes = "com.moulberry.flashback.record.Recorder", methods = "writeCustomSnapshot")
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public class FlashbackRecorderMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "writeCustomSnapshot", at = @At("HEAD"), remap = false)
    private void visor$writeVRSnapshot(Consumer<Packet<? super ClientGamePacketListener>> consumer, CallbackInfo ci) {
        if (ClientNetworking.CHANNEL == null) return;
        RecorderModHelper.collectInitPayloads(payload -> consumer.accept(
                (Packet<? super ClientGamePacketListener>) ModLoader.get()
                        .createPacketToClient(ClientNetworking.CHANNEL.getChannelId(), payload)
        ));
    }
}

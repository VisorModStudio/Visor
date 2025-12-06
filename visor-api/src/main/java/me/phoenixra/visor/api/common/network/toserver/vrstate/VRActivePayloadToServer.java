package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record VRActivePayloadToServer(boolean vrActive) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(vrActive);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.VR_ACTIVE;
    }


    public static VRActivePayloadToServer read(FriendlyByteBuf buffer) {
        return new VRActivePayloadToServer(
                buffer.readBoolean()
        );
    }
}

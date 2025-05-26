package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record VRActivePayloadToServer(boolean hasVr) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(hasVr);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.PLAYER_VR_ACTIVE;
    }


    public static VRActivePayloadToServer read(FriendlyByteBuf buffer) {
        return new VRActivePayloadToServer(
                buffer.readBoolean()
        );
    }
}

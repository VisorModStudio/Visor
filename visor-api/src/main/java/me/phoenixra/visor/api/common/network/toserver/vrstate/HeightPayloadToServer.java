package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record HeightPayloadToServer(float height) implements VisorPayloadToServer {

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.HEIGHT;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.height);
    }

    public static HeightPayloadToServer read(FriendlyByteBuf buffer) {
        return new HeightPayloadToServer(buffer.readFloat());
    }
}

package me.phoenixra.visor.api.common.network.toserver.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record RotationYPayloadToServer(float rotationY) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(rotationY);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.ROTATION_Y;
    }


    public static RotationYPayloadToServer read(FriendlyByteBuf buffer) {
        return new RotationYPayloadToServer(
                buffer.readFloat()
        );
    }
}

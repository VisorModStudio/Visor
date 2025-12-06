package me.phoenixra.visor.api.common.network.toclient.vrstate;

import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;


public record RotationYPayloadToClient(float rotationY) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(rotationY);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.ROTATION_Y;
    }


    public static RotationYPayloadToClient read(FriendlyByteBuf buffer) {
        return new RotationYPayloadToClient(
                buffer.readFloat()
        );
    }
}

package org.vmstudio.visor.api.common.network.toclient.vrstate;

import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;


public record RotationYPayloadToClient(float rotationY) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(rotationY);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.ROTATION_Y.byteOrdinal();
    }



    public static RotationYPayloadToClient read(FriendlyByteBuf buffer) {
        return new RotationYPayloadToClient(
                buffer.readFloat()
        );
    }
}

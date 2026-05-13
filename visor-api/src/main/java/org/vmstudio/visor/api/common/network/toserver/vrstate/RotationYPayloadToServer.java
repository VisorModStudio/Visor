package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record RotationYPayloadToServer(float rotationY) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(rotationY);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.ROTATION_Y.byteOrdinal();
    }



    public static RotationYPayloadToServer read(FriendlyByteBuf buffer) {
        return new RotationYPayloadToServer(
                buffer.readFloat()
        );
    }

}

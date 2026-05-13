package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record LeftHandedPayloadToServer(boolean leftHanded) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(leftHanded);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.LEFT_HANDED.byteOrdinal();
    }



    public static LeftHandedPayloadToServer read(FriendlyByteBuf buffer) {
        return new LeftHandedPayloadToServer(
                buffer.readBoolean()
        );
    }

}
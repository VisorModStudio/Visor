package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record ActiveHandPayloadToServer(boolean activeHandMain) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(activeHandMain);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.ACTIVE_HAND.byteOrdinal();
    }



    public static ActiveHandPayloadToServer read(FriendlyByteBuf buffer) {
        return new ActiveHandPayloadToServer(
                buffer.readBoolean()
        );
    }

}

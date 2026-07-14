package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record SeatedPayloadToServer(boolean seated) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(seated);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.SEATED.byteOrdinal();
    }



    public static SeatedPayloadToServer read(FriendlyByteBuf buffer) {
        return new SeatedPayloadToServer(
                buffer.readBoolean()
        );
    }

}

package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record FullHeightPayloadToServer(float fullHeight) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.fullHeight);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.FULL_HEIGHT.byteOrdinal();
    }



    public static FullHeightPayloadToServer read(FriendlyByteBuf buffer) {
        return new FullHeightPayloadToServer(buffer.readFloat());
    }

}

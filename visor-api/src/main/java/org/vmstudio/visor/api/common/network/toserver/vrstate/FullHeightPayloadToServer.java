package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record FullHeightPayloadToServer(float fullHeight) implements VisorPayloadToServer {

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.FULL_HEIGHT;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.fullHeight);
    }

    public static FullHeightPayloadToServer read(FriendlyByteBuf buffer) {
        return new FullHeightPayloadToServer(buffer.readFloat());
    }
}

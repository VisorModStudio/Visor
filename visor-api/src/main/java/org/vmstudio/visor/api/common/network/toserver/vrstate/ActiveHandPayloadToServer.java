package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record ActiveHandPayloadToServer(boolean activeHandMain) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(activeHandMain);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.ACTIVE_HAND;
    }


    public static ActiveHandPayloadToServer read(FriendlyByteBuf buffer) {
        return new ActiveHandPayloadToServer(
                buffer.readBoolean()
        );
    }
}

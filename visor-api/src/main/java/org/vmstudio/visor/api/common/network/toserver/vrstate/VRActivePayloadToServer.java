package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record VRActivePayloadToServer(boolean vrActive) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(vrActive);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.VR_ACTIVE;
    }


    public static VRActivePayloadToServer read(FriendlyByteBuf buffer) {
        return new VRActivePayloadToServer(
                buffer.readBoolean()
        );
    }
}

package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record LeftHandedPayloadToServer(boolean leftHanded) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(leftHanded);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.LEFT_HANDED;
    }


    public static LeftHandedPayloadToServer read(FriendlyByteBuf buffer) {
        return new LeftHandedPayloadToServer(
                buffer.readBoolean()
        );
    }
}
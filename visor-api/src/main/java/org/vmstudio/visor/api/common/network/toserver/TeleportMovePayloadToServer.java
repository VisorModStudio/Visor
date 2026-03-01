package org.vmstudio.visor.api.common.network.toserver;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record TeleportMovePayloadToServer(float x, float y, float z) implements VisorPayloadToServer {

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.TELEPORT;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
        buffer.writeFloat(this.z);
    }

    public static TeleportMovePayloadToServer read(FriendlyByteBuf buffer) {
        return new TeleportMovePayloadToServer(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }
}

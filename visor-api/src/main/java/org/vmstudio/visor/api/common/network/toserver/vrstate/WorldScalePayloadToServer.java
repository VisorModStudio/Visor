package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record WorldScalePayloadToServer(float worldScale)  implements VisorPayloadToServer {

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.WORLD_SCALE;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(worldScale);
    }

    public static WorldScalePayloadToServer read(FriendlyByteBuf buffer) {
        return new WorldScalePayloadToServer(buffer.readFloat());
    }
}

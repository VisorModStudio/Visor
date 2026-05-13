package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record WorldScalePayloadToServer(float worldScale)  implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(worldScale);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.WORLD_SCALE.byteOrdinal();
    }



    public static WorldScalePayloadToServer read(FriendlyByteBuf buffer) {
        return new WorldScalePayloadToServer(buffer.readFloat());
    }

}

package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record GunAnglePayloadToServer(float gunAngle) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(gunAngle);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.GUN_ANGLE.byteOrdinal();
    }



    public static GunAnglePayloadToServer read(FriendlyByteBuf buffer) {
        return new GunAnglePayloadToServer(buffer.readFloat());
    }

}

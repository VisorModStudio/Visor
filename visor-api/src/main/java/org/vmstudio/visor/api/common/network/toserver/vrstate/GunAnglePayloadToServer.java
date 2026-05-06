package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record GunAnglePayloadToServer(float gunAngle) implements VisorPayloadToServer {

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.GUN_ANGLE;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeFloat(gunAngle);
    }

    public static GunAnglePayloadToServer read(FriendlyByteBuf buffer) {
        return new GunAnglePayloadToServer(buffer.readFloat());
    }
}

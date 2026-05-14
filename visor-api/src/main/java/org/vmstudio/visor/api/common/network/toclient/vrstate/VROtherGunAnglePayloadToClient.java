package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherGunAnglePayloadToClient(UUID playerUUID,
                                            float gunAngle) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeFloat(gunAngle);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_GUN_ANGLE.byteOrdinal();
    }



    public static VROtherGunAnglePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherGunAnglePayloadToClient(
                buffer.readUUID(),
                buffer.readFloat()
        );
    }
}

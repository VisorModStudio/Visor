package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherGunAnglePayloadToClient(UUID playerUUID,
                                            float gunAngle) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeFloat(gunAngle);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_GUN_ANGLE.byteOrdinal();
    }



    public static VROtherGunAnglePayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherGunAnglePayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherGunAnglePayloadToClient(
                uuid,
                buffer.readFloat()
        );
    }
}

package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherOverlayFocusedPayloadToClient(UUID playerUUID, boolean overlayFocused) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeBoolean(overlayFocused);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_OVERLAY_FOCUSED.byteOrdinal();
    }

    public static VROtherOverlayFocusedPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherOverlayFocusedPayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {

        return new VROtherOverlayFocusedPayloadToClient(
                uuid,
                buffer.readBoolean()
        );
    }
}
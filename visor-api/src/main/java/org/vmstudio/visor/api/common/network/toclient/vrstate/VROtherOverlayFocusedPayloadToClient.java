package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherOverlayFocusedPayloadToClient(UUID playerUUID, boolean overlayFocused) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(overlayFocused);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_OVERLAY_FOCUSED.byteOrdinal();
    }

    public static VROtherOverlayFocusedPayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherOverlayFocusedPayloadToClient(buffer.readUUID(), buffer.readBoolean());
    }
}
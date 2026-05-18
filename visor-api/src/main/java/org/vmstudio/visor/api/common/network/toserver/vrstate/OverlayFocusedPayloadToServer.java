package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

public record OverlayFocusedPayloadToServer(boolean overlayFocused) implements VisorPayloadToServer {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(overlayFocused);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OVERLAY_FOCUSED.byteOrdinal();
    }

    public static OverlayFocusedPayloadToServer read(FriendlyByteBuf buffer) {
        return new OverlayFocusedPayloadToServer(buffer.readBoolean());
    }
}
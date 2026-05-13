package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record OffhandSlotPayloadToServer(int slot) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OFFHAND_SLOT.byteOrdinal();
    }



    public static OffhandSlotPayloadToServer read(FriendlyByteBuf buffer) {
        return new OffhandSlotPayloadToServer(
                buffer.readInt()
        );
    }
}


package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

public record OffhandSlotPayloadToClient(int slot) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OFFHAND_SLOT.byteOrdinal();
    }



    public static OffhandSlotPayloadToClient read(FriendlyByteBuf buffer) {
        return new OffhandSlotPayloadToClient(
                buffer.readInt()
        );
    }
}

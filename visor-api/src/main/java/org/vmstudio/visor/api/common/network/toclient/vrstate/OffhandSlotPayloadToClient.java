package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;

public record OffhandSlotPayloadToClient(int slot) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OFFHAND_SLOT;
    }


    public static OffhandSlotPayloadToClient read(FriendlyByteBuf buffer) {
        return new OffhandSlotPayloadToClient(
                buffer.readInt()
        );
    }
}

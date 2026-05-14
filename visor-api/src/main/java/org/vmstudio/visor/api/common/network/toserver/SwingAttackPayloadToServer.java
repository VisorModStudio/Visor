package org.vmstudio.visor.api.common.network.toserver;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record SwingAttackPayloadToServer(int entityId,
                                         boolean shiftKeyDown,
                                         boolean mainHand) implements VisorPayloadToServer{

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeBoolean(shiftKeyDown);
        buffer.writeBoolean(mainHand);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.SWING_ATTACK.byteOrdinal();
    }



    public static SwingAttackPayloadToServer read(FriendlyByteBuf buffer) {
        return new SwingAttackPayloadToServer(
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }
}

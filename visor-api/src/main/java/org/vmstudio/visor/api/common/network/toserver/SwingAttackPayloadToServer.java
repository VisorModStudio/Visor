package org.vmstudio.visor.api.common.network.toserver;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.vmstudio.visor.api.common.network.VisorPayloadID;

public record SwingAttackPayloadToServer(int entityId,
                                         boolean shiftKeyDown,
                                         boolean mainHand) implements VisorPayloadToServer{
    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.SWING_ATTACK;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeBoolean(shiftKeyDown);
        buffer.writeBoolean(mainHand);
    }

    public static SwingAttackPayloadToServer read(FriendlyByteBuf buffer) {
        return new SwingAttackPayloadToServer(
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }
}

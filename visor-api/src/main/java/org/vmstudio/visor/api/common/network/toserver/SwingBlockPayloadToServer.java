package org.vmstudio.visor.api.common.network.toserver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;

public record SwingBlockPayloadToServer(BlockPos blockPos,
                                        Direction direction,
                                        boolean mainHand,
                                        int sequence) implements VisorPayloadToServer {
    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.SWING_BLOCK;
    }

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeByte(direction.get3DDataValue());
        buffer.writeBoolean(mainHand);
        buffer.writeInt(sequence);
    }

    public static SwingBlockPayloadToServer read(FriendlyByteBuf buffer) {
        return new SwingBlockPayloadToServer(
                buffer.readBlockPos(),
                Direction.from3DDataValue(buffer.readUnsignedByte()),
                buffer.readBoolean(),
                buffer.readInt()
        );
    }
}

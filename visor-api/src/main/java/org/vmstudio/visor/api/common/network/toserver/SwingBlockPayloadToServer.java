package org.vmstudio.visor.api.common.network.toserver;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record SwingBlockPayloadToServer(BlockPos blockPos,
                                        Direction direction,
                                        boolean mainHand,
                                        int sequence) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeByte(direction.get3DDataValue());
        buffer.writeBoolean(mainHand);
        buffer.writeInt(sequence);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.SWING_BLOCK.byteOrdinal();
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

package org.vmstudio.visor.api.common.network.toclient;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record BlockDamagePayloadToClient(UUID playerUUID,
                                         BlockPos blockPos,
                                         int destroyStage) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBlockPos(blockPos);
        buffer.writeInt(destroyStage);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.BLOCK_DAMAGE.byteOrdinal();
    }



    public static BlockDamagePayloadToClient read(FriendlyByteBuf buffer) {
        return new BlockDamagePayloadToClient(
                buffer.readUUID(),
                buffer.readBlockPos(),
                buffer.readInt()
        );
    }
}

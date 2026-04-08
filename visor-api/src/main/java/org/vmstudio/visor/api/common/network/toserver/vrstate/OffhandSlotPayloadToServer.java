package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record OffhandSlotPayloadToServer(int slot) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OFFHAND_SLOT;
    }


    public static OffhandSlotPayloadToServer read(FriendlyByteBuf buffer) {
        return new OffhandSlotPayloadToServer(
                buffer.readInt()
        );
    }
}


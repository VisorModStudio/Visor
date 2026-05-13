package org.vmstudio.visor.api.common.network.toclient;

import com.google.common.base.Charsets;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

public record SettingsPayloadToClient(String config) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(
                config.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.SERVER_SETTINGS.byteOrdinal();
    }



    public static SettingsPayloadToClient read(FriendlyByteBuf buffer) {

        return new SettingsPayloadToClient(
                VisorPayload.readString(buffer)
        );
    }
}

package org.vmstudio.visor.api.common.network.toclient;

import com.google.common.base.Charsets;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record SettingsPayloadToClient(String config) implements VisorPayloadToClient{
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(
                config.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.SETTINGS;
    }


    public static VisorPayloadToClient read(FriendlyByteBuf buffer) {

        return new SettingsPayloadToClient(
                VisorPayload.readString(buffer)
        );
    }
}

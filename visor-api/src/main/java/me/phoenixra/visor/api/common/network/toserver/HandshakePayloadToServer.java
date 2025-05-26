package me.phoenixra.visor.api.common.network.toserver;

import com.google.common.base.Charsets;
import me.phoenixra.visor.api.common.network.VisorPayload;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record HandshakePayloadToServer(String version,
                                       boolean vrActive,
                                       int maxVersion,
                                       int minVersion) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(
                String.format("%s %s\n%d\n%d",
                                this.version,
                                this.vrActive ? "on" : "off",
                                this.maxVersion,
                                this.minVersion
                        )
                        .getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.HANDSHAKE;
    }


    public static VisorPayloadToServer read(FriendlyByteBuf buffer) {

        String[] parts = VisorPayload
                .readString(buffer)
                .split("\\n");

        boolean vr = !parts[0].contains("off");
        return new HandshakePayloadToServer(
                parts[0],
                vr,
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );
    }
}

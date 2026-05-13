package org.vmstudio.visor.api.common.network.toserver.vrstate;


import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public record CrawlingPayloadToServer(boolean crawling) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(crawling);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.CRAWLING.byteOrdinal();
    }



    public static CrawlingPayloadToServer read(FriendlyByteBuf buffer) {
        return new CrawlingPayloadToServer(
                buffer.readBoolean()
        );
    }

}


package me.phoenixra.visor.api.common.network.toserver.vrstate;


import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;

public record CrawlingPayloadToServer(boolean crawling) implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(crawling);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.CRAWLING;
    }


    public static CrawlingPayloadToServer read(FriendlyByteBuf buffer) {
        return new CrawlingPayloadToServer(
                buffer.readBoolean()
        );
    }
}


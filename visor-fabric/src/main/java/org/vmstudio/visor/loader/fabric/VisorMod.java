package org.vmstudio.visor.loader.fabric;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.vmstudio.visor.loader.fabric.network.VisorRawPayload;

public class VisorMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // register the single network tunnel payload once, during init —
        // the play registries reject late or duplicate registrations
        PayloadTypeRegistry.playC2S().register(VisorRawPayload.TYPE, VisorRawPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(VisorRawPayload.TYPE, VisorRawPayload.STREAM_CODEC);
    }
}

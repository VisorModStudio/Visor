package org.vmstudio.visor.api.common.network;


import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VisorNetwork {
    private VisorNetwork() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static final ResourceLocation CORE_CHANNEL_ID = new ResourceLocation("visor:channel");

    public static final int CORE_NETWORK_VERSION = 5; // 5: since Visor 0.5.0


    private static final Map<ResourceLocation, VisorChannel> CHANNELS = new ConcurrentHashMap<>();


    public static void registerChannel(@NotNull VisorChannel channel) {
        if (CHANNELS.putIfAbsent(channel.getChannelId(), channel) != null) {
            throw new IllegalStateException(
                    "VisorAddonChannel already registered: " + channel.getChannelId());
        }
        ModLoader.get().registerNetworkChannel(channel);
    }

    public static @Nullable VisorChannel getChannel(@NotNull ResourceLocation id) {
        return CHANNELS.get(id);
    }

    public static @NotNull Map<ResourceLocation, VisorChannel> getAllChannels() {
        return Collections.unmodifiableMap(CHANNELS);
    }

}

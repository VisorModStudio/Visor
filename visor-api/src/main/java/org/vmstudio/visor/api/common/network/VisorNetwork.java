package org.vmstudio.visor.api.common.network;


import net.minecraft.resources.ResourceLocation;

public interface VisorNetwork {
    ResourceLocation CHANNEL = new ResourceLocation("visor:channel");

    int NETWORK_VERSION = 3; // 3 since Visor 0.3.0


}

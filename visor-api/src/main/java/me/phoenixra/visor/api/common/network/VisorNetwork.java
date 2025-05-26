package me.phoenixra.visor.api.common.network;


import net.minecraft.resources.ResourceLocation;

public interface VisorNetwork {
    ResourceLocation CHANNEL = new ResourceLocation("visor:channel");

    int NETWORK_VERSION = 1;
    int MIN_NETWORK_VERSION = 1;
    int MAX_NETWORK_VERSION = 1;


}

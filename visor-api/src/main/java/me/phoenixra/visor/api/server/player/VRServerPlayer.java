package me.phoenixra.visor.api.server.player;


import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRServerPlayer {
    @NotNull
    ServerPlayer getMcPlayer();

    PlayerPoseServer getPoseData();

    boolean isCrawling();
    float getHeight();
    float getBowTension();

    boolean isVRActive();
}

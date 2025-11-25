package me.phoenixra.visor.api.client.player;

import me.phoenixra.visor.api.client.player.pose.PlayerPoseClient;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import org.jetbrains.annotations.NotNull;

public interface VRClientPlayer {

    @NotNull
    PlayerPoseClient getPoseData(@NotNull PlayerPoseType type);

    float getHeight();

    default float getHeightScale() {

        return getHeight() / 1.52f;
    }


    boolean isLeftHanded();
}

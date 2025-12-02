package me.phoenixra.visor.api.client.player;

import me.phoenixra.visor.api.client.player.pose.PlayerPoseClient;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.common.player.PoseHistory;
import org.jetbrains.annotations.NotNull;

public interface VRClientPlayer {

    @NotNull
    PlayerPoseClient getPoseData(@NotNull PlayerPoseType type);

    @NotNull
    PoseHistory getPoseHistoryRelative();

    @NotNull
    PoseHistory getPoseHistoryTick();


    float getFullHeight();

    default float getActualHeight(){
        return getPoseData(PlayerPoseType.RELATIVE).getHeadPivot().y();
    }

    default float getFullHeightScale() {

        return getFullHeight() / 1.52f;
    }


    boolean isLeftHanded();
}

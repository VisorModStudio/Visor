package org.vmstudio.visor.api.server.player;


import org.vmstudio.visor.api.common.player.VRPoseHistory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRServerPlayer {
    @NotNull
    ServerPlayer getMcPlayer();

    PlayerPoseServer getPoseDataPrev();
    PlayerPoseServer getPoseData();
    PlayerPoseServer getPoseDataRelative();

    @NotNull
    VRPoseHistory getPoseHistoryRelative();

    @NotNull
    VRPoseHistory getPoseHistoryTick();

    boolean isCrawling();

    float getFullHeight();
    default float getActualHeight(){
        return getPoseData().getHeadPivot().y();
    }
    float getBowTension();

    boolean isVRActive();
}

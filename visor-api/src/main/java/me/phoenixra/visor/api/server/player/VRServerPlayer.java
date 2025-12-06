package me.phoenixra.visor.api.server.player;


import me.phoenixra.visor.api.common.player.PoseHistory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRServerPlayer {
    @NotNull
    ServerPlayer getMcPlayer();

    PlayerPoseServer getPoseDataPrev();
    PlayerPoseServer getPoseData();
    PlayerPoseServer getPoseDataRelative();

    @NotNull
    PoseHistory getPoseHistoryRelative();

    @NotNull
    PoseHistory getPoseHistoryTick();

    boolean isCrawling();

    float getFullHeight();
    default float getActualHeight(){
        return getPoseData().getHeadPivot().y();
    }
    float getBowTension();

    boolean isVRActive();
}

package org.vmstudio.visor.api.server.player;


import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.player.VRPoseHistory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRServerPlayer extends VRPlayer {
    @NotNull
    ServerPlayer getMcPlayer();

    @NotNull PlayerPoseServer getPosePrevious();
    @NotNull PlayerPoseServer getPoseRelative();
    @NotNull PlayerPoseServer getPose();


    boolean isCrawling();

}

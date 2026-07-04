package org.vmstudio.visor.api.server.player;

import org.vmstudio.visor.api.common.player.VRPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRServerPlayer extends VisorServerPlayer, VRPlayer {


    @NotNull PlayerPoseServer getPoseDataPrevious();
    @NotNull PlayerPoseServer getPoseDataRoom();
    @NotNull PlayerPoseServer getPoseData();


    boolean isCrawling();

}

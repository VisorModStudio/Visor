package org.vmstudio.visor.api.client.player;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.RawController;
import org.vmstudio.visor.api.client.player.pose.RawHmd;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRLocalPlayer extends VRClientPlayer{

    /**
     * Get local player associated with this instance
     *
     * @return mc player
     */
    LocalPlayer getMcPlayer();

    /**
     * Get hand type which is currently used
     * by player for attack/mining
     *
     * @return hand type
     */
    @NotNull
    HandType getActiveHand();

    /**
     * Get pose that affects client rotation
     * @return pose
     */
    @NotNull VRPose getRotationElement(@NotNull PlayerPoseType poseType);

    /**
     * If player is crawling
     *
     * @return true/false
     */
    boolean isCrawling();

    /**
     * If player is climbing with hands
     *
     * @return true/false
     */
    boolean isClimbing();

    /**
     * If player is climbing with specified hand
     *
     * @return true/false
     */
    boolean isClimbing(@NotNull HandType handType);


    /**
     * Get Raw Hmd
     *
     * @return RawHmd instance
     */
    RawHmd getRawHmd();

    /**
     * Get Raw Controller for specified hand type
     *
     * @param type the hand type
     * @return RawController instance
     */
    RawController getRawController(@NotNull HandType type);


}

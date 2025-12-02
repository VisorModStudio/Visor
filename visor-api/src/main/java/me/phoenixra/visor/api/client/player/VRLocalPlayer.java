package me.phoenixra.visor.api.client.player;

import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.player.pose.RawController;
import me.phoenixra.visor.api.client.player.pose.RawHmd;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.player.PoseElement;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

public interface VRLocalPlayer extends VRClientPlayer{

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


    /**
     * Get pose element that affects client rotation
     * @return pose element
     */
    @NotNull PoseElement getRotationElement(@NotNull PlayerPoseType stage);

}

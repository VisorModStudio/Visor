package me.phoenixra.visor.api.client.player;

import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
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
     * Get component that affects client rotation
     * @return component
     */
    @NotNull PoseElement getRotationElement(@NotNull PlayerPoseType stage);

}

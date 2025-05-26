package me.phoenixra.visor.api.client;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.IVRClientPose;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class handles client-specific VR data
 * and in-game staff for player
 * <br><br>
 * It is not bound to a world, like MC player.
 * So, once VR initialized, an instance
 * of this class becomes available
 *
 */
public interface ClientPlayer {



    @NotNull
    IVRClientPose getPose(@NotNull PoseType stage);

    /**
     * Get ControllerHand type which is currently used
     * by player for attack/mining
     *
     * @return ControllerHand type
     */
    @NotNull
    ControllerHand getActiveHand();

    /**
     * Returns global world scale,
     * which is used in player poses
     *
     * @return world scale
     */
    float getWorldScale();



    /**
     * Get component that affects client rotation
     * @return component
     */
    @NotNull PoseElement getRotationElement(@NotNull PoseType stage);

    @Nullable
    Player getMcPlayer();

}

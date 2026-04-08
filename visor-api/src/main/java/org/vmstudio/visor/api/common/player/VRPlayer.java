package org.vmstudio.visor.api.common.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.HandType;

/**
 * The common interface for VR players, both client and server side
 */
public interface VRPlayer {

    Player getMcPlayer();

    @NotNull
    VRPlayerPose getPosePrevious();

    @NotNull
    VRPlayerPose getPoseRelative();

    @NotNull
    VRPlayerPose getPose();


    /**
     * get pose history for relative type
     *
     * @return pose history
     */
    @NotNull
    VRPoseHistory getPoseHistoryRelative();

    /**
     * get pose history for tick type
     *
     * @return pose history
     */
    @NotNull
    VRPoseHistory getPoseHistoryTick();


    int getOffhandSlot();

    /**
     * If this VR player is left-handed
     *
     * @return true/false
     */
    boolean isLeftHanded();

    /**
     * Get hand type which is currently used
     * by player for attack/mining
     *
     * @return hand type
     */
    @NotNull
    HandType getActiveHand();

    /**
     * Get full height
     *
     * @return full height
     */
    float getFullHeight();

    /**
     * Get actual height
     *
     * @return actual height
     */
    default float getActualHeight(){
        return getPose().getHeadPivot().y();
    }

    /**
     * Get full height scale.
     * <p>
     *     It is the ratio between {@link #getFullHeight()}
     *     and height of a minecraft player
     * </p>
     *
     * @return full height scale
     */
    default float getFullHeightScale() {

        return getFullHeight() / 1.52f;
    }

}

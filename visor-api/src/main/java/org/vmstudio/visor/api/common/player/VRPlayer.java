package org.vmstudio.visor.api.common.player;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.HandType;

/**
 * The common interface for VR players, both client and server side
 */
public interface VRPlayer extends VisorPlayer{

    float DEFAULT_GUN_ANGLE = 60.0F;
    float DEFAULT_FULL_HEIGHT = 1.52f;

    @NotNull
    VRPlayerPose getPoseDataPrevious();

    @NotNull
    VRPlayerPose getPoseDataRelative();

    @NotNull
    VRPlayerPose getPoseData();


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

    /**
     * Whether this is a remote player, i.e. another player
     * tracked on the local client.
     *
     * @return true/false
     */
    default boolean isRemote() {
        return false;
    }

    /**
     * The gun angle is used for item pose
     * compatibility with different controllers.
     *
     * @return the gun angle
     */
    float getGunAngle();


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
        return getPoseDataRelative().getHeadPivot().y();
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

        return getFullHeight() / DEFAULT_FULL_HEIGHT;
    }

    /**
     * Whether the player currently has an overlay
     *
     * @return true when an overlay is active
     */
    boolean isOverlayFocused();


}

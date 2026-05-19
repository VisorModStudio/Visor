package org.vmstudio.visor.api.client.player;


import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.player.pose.RawController;
import org.vmstudio.visor.api.client.player.pose.RawHmd;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.common.player.VRPose;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.server.VRServerSettings;

public interface VRLocalPlayer extends VRClientPlayer{

    /**
     * Get local player associated with this instance
     *
     * @return mc player
     */
    @Nullable LocalPlayer getMcPlayer();

    /**
     * Set VR body to use.
     *
     * @param vrBody the VR body
     */
    void setBodyType(@NotNull VRBodyType vrBody);

    /**
     * Set if player can change the selected VR body.
     * <p>
     * On false, the VR body selection will be blocked for player
     *
     * @param flag true/false
     */
    void setBodyChangeable(boolean flag);

    /**
     * If player can change the selected VR body.
     * @return true/false
     */
    boolean isBodyChangeable();


    /**
     * Set active hand type
     */
    void setActiveHand(@NotNull HandType handType);

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


    /**
     * Set the inventory hotbar slot used as the VR offhand slot.
     * <p>
     *     Only applies when two-handed VR gameplay is supported
     *     ({@link VRServerSettings#isTwoHandedVR()}).
     * </p>
     *
     * @param slot hotbar slot index in {@code [0, 9)}, or {@code -1} to clear
     * @see VRPlayer#getOffhandSlot()
     */
    void setOffhandSlot(int slot);

}

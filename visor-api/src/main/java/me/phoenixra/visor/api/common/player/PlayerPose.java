package me.phoenixra.visor.api.common.player;

import me.phoenixra.visor.api.common.HandType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;


/**
 * Provides pose data for VR player
 */
public interface PlayerPose {

    Player getMcPlayer();

    /**
     * Get the head-mounted display (HMD) pose element.
     *
     * @return the HMD pose element
     */
    @NotNull
    PoseElement getHmd();


    /**
     * Get the main hand pose element.
     * <p>
     *   This represents the aiming pose of VR controller.
     * </p>
     *
     * @return the main hand pose element
     */
    @NotNull
    PoseElement getMainHand();

    /**
     * Get the offhand pose element.
     * <p>
     *   This represents the aiming pose of VR controller.
     * </p>
     *
     * @return the offhand pose element
     */
    @NotNull
    PoseElement getOffhand();



    /**
     * Get the origin to which pose elements relative to.
     * <p>
     *   The origin is used by Visor to convert
     *   pose data in VR room coordinates
     *   to world coordinates relative to player
     * </p>
     *
     * @return the origin
     */
    @NotNull
    Vector3fc getOrigin();

    /**
     * Get the world scale.
     * <p>
     *   This defines how VR units map to world units.
     * </p>
     *
     * @return the world scale factor
     */
    float getWorldScale();

    /**
     * Get the player rotation Y in radians.
     * <p>
     *   This is the general yaw rotation for VR.
     *   We don't use the yaw from minecraft
     *   to have a protection layer from motion sickness
     *   between VR and minecraft logic made for PCs.
     *   The other reason is to be able to control
     *   player model/ray tracing without interfering VR
     * </p>
     *
     * @return the player's rotation Y in radians
     */
    float getRotationY();


    /**
     * Get the head pivot point.
     * <p>
     *   This is an approximate point around which the head rotates,
     *   i.e. the position of the player's neck.
     * </p>
     *
     * @return the head pivot position
     */
    @NotNull
    Vector3fc getHeadPivot();

    /**
     * Get the body yaw in radians.
     * <p>
     *   This is the rotation of the body around the Y axis,
     *   which may differ from the head rotation.
     * </p>
     *
     * @return the body yaw in radians
     */
    float getBodyYaw();





    /**
     * Get the hand pose element for the given hand type.
     * <p>
     *   This represents the aiming pose of VR controller
     * </p>
     *
     * @param handType the hand type
     * @return the hand pose element
     */
    @NotNull
    default PoseElement getHand(@NotNull HandType handType) {
        return handType == HandType.MAIN
                ? getMainHand() : getOffhand();
    }




    void resetOrigin(Vector3fc newOrigin);

}

package me.phoenixra.visor.api.client.player.pose;

import me.phoenixra.visor.api.client.player.VRRemotePlayer;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.player.PlayerPose;
import me.phoenixra.visor.api.common.player.PoseElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Provides pose data for the client-side
 * VR player
 */
public interface PlayerPoseClient extends PlayerPose {

    /**
     * Get the pose type used by this instance.
     *
     * @return the pose data type
     */
    PlayerPoseType getType();


    /**
     * Get the left eye pose element.
     * <p>
     *   For {@link VRRemotePlayer remote player} returns {@link #getHmd()} instead
     * </p>
     *
     * @return the left eye pose element
     */
    @NotNull
    PoseElement getEyeLeft();

    /**
     * Get the right eye pose element.
     * <p>
     *   For {@link VRRemotePlayer remote player} returns {@link #getHmd()} instead
     * </p>
     *
     * @return the right eye pose element
     */
    @NotNull
    PoseElement getEyeRight();

    /**
     * Get the main hand grip pose element.
     * <p>
     *   This represents the gripping pose of VR controller.
     *   For {@link VRRemotePlayer remote player} returns {@link #getMainHand()} instead
     * </p>
     *
     * @return the main hand grip pose element
     */
    @NotNull
    PoseElement getGripMainHand();

    /**
     * Get the offhand grip pose element.
     * <p>
     *   This represents the gripping pose of VR controller.
     *   For {@link VRRemotePlayer remote player} returns {@link #getOffhand()} instead
     * </p>
     *
     * @return the offhand grip pose element
     */
    @NotNull
    PoseElement getGripOffhand();

    /**
     * Get the grip hand pose element for the given hand type.
     * <p>
     *   This represents the gripping pose of VR controller
     * </p>
     *
     * @param handType the hand type
     * @return the grip hand pose element
     */
    @NotNull
    default PoseElement getGripHand(@NotNull HandType handType) {
        return handType == HandType.MAIN
                ? getGripMainHand() : this.getGripOffhand();
    }

    /**
     * Get the pose element for camera type
     *
     * @param cameraType the camera type
     * @return the pose element
     */
    @NotNull
    PoseElement getCameraPose(@Nullable VRCameraType cameraType);

    /**
     * Converts <code>position</code> from the coordinate system of <code>originType</code>
     * to the coordinate system of this player pose instance.
     *
     *
     * @param originType the stage from which the position is defined
     * @param position the position vector in the coordinate system of the specified origin stage
     * @return the converted position vector
     */
    @NotNull Vector3f convertPositionFrom(@NotNull PlayerPoseType originType,
                                          @NotNull Vector3fc position);

    /**
     * Converts <code>rotationMatrix</code> from the coordinate system of <code>originType</code>
     * to the coordinate system of this player pose instance.
     *
     *
     * @param originType the stage from which the rotation matrix is defined
     * @param rotationMatrix the rotation matrix to convert
     * @return the converted rotation matrix
     */
    @NotNull Matrix4f convertRotationFrom(@NotNull PlayerPoseType originType,
                                          @NotNull Matrix4fc rotationMatrix);
}

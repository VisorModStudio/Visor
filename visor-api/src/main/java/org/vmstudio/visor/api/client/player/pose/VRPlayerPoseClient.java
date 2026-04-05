package org.vmstudio.visor.api.client.player.pose;

import org.vmstudio.visor.api.client.player.VRRemotePlayer;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPlayerPose;
import org.vmstudio.visor.api.common.player.VRPose;
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
public interface VRPlayerPoseClient extends VRPlayerPose {

    /**
     * Get the pose type used by this instance.
     *
     * @return the pose data type
     */
    PlayerPoseType getType();

    /**
     * Get VR body
     * @return the VR body
     */
    VRBody getBody();

    /**
     * Get the left eye pose.
     * <p>
     *   For {@link VRRemotePlayer remote player} returns {@link #getHmd()} instead
     * </p>
     *
     * @return the left eye pose
     */
    @NotNull
    VRPose getEyeLeft();

    /**
     * Get the right eye pose.
     * <p>
     *   For {@link VRRemotePlayer remote player} returns {@link #getHmd()} instead
     * </p>
     *
     * @return the right eye pose
     */
    @NotNull
    VRPose getEyeRight();

    /**
     * Get the main hand grip pose.
     * <p>
     *   This represents the gripping pose of VR controller.
     *   For {@link VRRemotePlayer remote player} returns {@link #getMainHand()} instead
     * </p>
     *
     * @return the main hand grip pose
     */
    @NotNull
    VRPose getGripMainHand();

    /**
     * Get the offhand grip pose.
     * <p>
     *   This represents the gripping pose of VR controller.
     *   For {@link VRRemotePlayer remote player} returns {@link #getOffhand()} instead
     * </p>
     *
     * @return the offhand grip pose
     */
    @NotNull
    VRPose getGripOffhand();

    /**
     * Get the grip hand pose for the given hand type.
     * <p>
     *   This represents the gripping pose of VR controller
     * </p>
     *
     * @param handType the hand type
     * @return the grip hand pose
     */
    @NotNull
    default VRPose getGripHand(@NotNull HandType handType) {
        return handType == HandType.MAIN
                ? getGripMainHand() : this.getGripOffhand();
    }

    /**
     * Get the pose for render pass
     *
     * @param renderPass the render pass
     * @return the pose
     */
    @NotNull
    VRPose getCameraPose(@Nullable VRRenderPass renderPass);

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

package org.vmstudio.visor.api.common.player;


import org.joml.Vector3f;


import java.util.List;

/**
 * Represents a bounded history of player poses sampled each tick.
 * <p>
 * Implementations treat index {@code 0} as the most recent pose,
 * with increasing indices going further back in time.
 */
public interface VRPoseHistory {

    /**
     * Maximum number of pose samples that can be stored in the history.
     */
    int HISTORY_LIMIT = 100;

    /**
     * Returns an immutable snapshot of all stored pose samples.
     *
     * @return list of player poses ordered from newest to oldest
     */
    List<VRPlayerPose> getAllHistory();

    /**
     * Returns a pose from the history by its age in ticks.
     *
     * @param ticksBack how many ticks back to look, where {@code 0} is the newest pose
     * @return the pose at the given offset
     * @throws IndexOutOfBoundsException if {@code ticksBack} is outside the stored range
     */
    VRPlayerPose getEntry(int ticksBack);

    /**
     * Returns the number of stored pose samples.
     *
     * @return current history size
     */
    int getHistorySize();

    /**
     * Computes the net movement of a body part over the specified time window.
     *
     * @param bodyPart    body part to measure
     * @param maxTicksBack maximum ticks back to include, starting from the newest pose
     * @return displacement vector from the older pose to the newest pose;
     *         a zero vector if there is not enough data
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    Vector3f netMovement(VRTrackableBodyPart bodyPart,
                         int maxTicksBack);

    /**
     * Computes the net movement of the head pivot over the specified time window.
     *
     * @param maxTicksBack maximum ticks back to include, starting from the newest pose
     * @return displacement vector of the head pivot from the older pose to the newest pose;
     *         a zero vector if there is not enough data
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    Vector3f headPivotNetMovement(int maxTicksBack);

    /**
     * Computes the average per-tick movement speed of a body part.
     * <p>
     * Speed is calculated over up to {@code maxTicksBack} most recent ticks.
     *
     * @param bodyPart    body part to measure
     * @param maxTicksBack maximum number of ticks to consider
     * @return average movement distance per tick; {@code 0} if there is not enough data
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    double averageSpeed(VRTrackableBodyPart bodyPart,
                        int maxTicksBack);

    /**
     * Computes the average per-tick movement speed of the head pivot.
     * <p>
     * Speed is calculated over up to {@code maxTicksBack} most recent ticks.
     *
     * @param maxTicksBack maximum number of ticks to consider
     * @return average head pivot movement distance per tick; {@code 0} if there is not enough data
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    double headPivotAverageSpeed(int maxTicksBack);

    /**
     * Computes the average position of a body part over the specified time window.
     *
     * @param bodyPart    body part to sample
     * @param maxTicksBack maximum number of most recent poses to include
     * @return average position of the body part, or {@code null} if history is empty
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    Vector3f averagePosition(VRTrackableBodyPart bodyPart,
                             int maxTicksBack);

    /**
     * Computes the average head pivot position over the specified time window.
     *
     * @param maxTicksBack maximum number of most recent poses to include
     * @return average head pivot position, or {@code null} if history is empty
     * @throws IllegalArgumentException if {@code maxTicksBack} is negative
     *                                  or greater than {@link #HISTORY_LIMIT}
     */
    Vector3f headPivotAveragePosition(int maxTicksBack);
}

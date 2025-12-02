package me.phoenixra.visor.api.common.player;


import org.joml.Vector3f;


import java.util.List;

/**
 * Represents tick history of player poses
 */
public interface PoseHistory {

    int HISTORY_LIMIT = 100;


    List<PlayerPose> getAllHistory();

    PlayerPose getEntry(int ticksBack);

    int getHistorySize();


    Vector3f netMovement(VRBodyPart bodyPart,
                         int maxTicksBack);

    Vector3f headPivotNetMovement(int maxTicksBack);

    double averageSpeed(VRBodyPart bodyPart,
                        int maxTicksBack);

    double headPivotAverageSpeed(int maxTicksBack);
}

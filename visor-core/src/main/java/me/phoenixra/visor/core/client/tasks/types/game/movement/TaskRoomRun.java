package me.phoenixra.visor.core.client.tasks.types.game.movement;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.PoseDataImpl;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVisorTask
public class TaskRoomRun extends VisorTask {
    private static final String ID = "room_run";
    @Getter
    private static TaskRoomRun instance;

    private static final double RUNNING_SPEED_THRESHOLD = 0.1D;
    private static final double IDLE_SPEED_THRESHOLD = 0.6D;
    private static final double HAND_SPEED_DIFF_MAX = 0.5D;
    private static final double SPEED_MULTIPLIER = 1.3D;

    private static final double FULL_SPEED_THRESHOLD = 0.1D;


    @Getter
    private double direction = 0.0D;
    @Getter
    private double speed = 0.0D;

    public TaskRoomRun(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {

        final double rightHandSpeed = ClientContext.rawPoseHandler.getControllerRightData()
                .getPositionHistory().averageSpeed(0.33D);
        final double leftHandSpeed = ClientContext.rawPoseHandler.getControllerLeftData()
                .getPositionHistory().averageSpeed(0.33D);

        if (this.speed > 0) {
            if (rightHandSpeed < RUNNING_SPEED_THRESHOLD
                    && leftHandSpeed < RUNNING_SPEED_THRESHOLD) {
                this.speed = 0.0D;
                return;
            }
        } else if (rightHandSpeed < IDLE_SPEED_THRESHOLD
                && leftHandSpeed < IDLE_SPEED_THRESHOLD) {
            this.speed = 0.0D;
            return;
        }

        if (Math.abs(rightHandSpeed - leftHandSpeed) > HAND_SPEED_DIFF_MAX) {
            this.speed = 0.0D;
            return;
        }

        PoseDataImpl preTickPose = ClientContext.player
                .getPose(PoseType.PRE_TICK);

        final Vec3 mainHandDir = preTickPose
                .getController(ControllerHand.MAIN)
                .getDirection();
        final Vec3 offhandDir = preTickPose
                .getController(ControllerHand.OFFHAND)
                .getDirection();
        final Vec3 directionAvg = mainHandDir.add(offhandDir).scale(0.5D);

        this.direction = Math.toDegrees(Mth.atan2(-directionAvg.x, directionAvg.z));

        final double speedAvg = (rightHandSpeed + leftHandSpeed) / 2.0D;
        this.speed = speedAvg * SPEED_MULTIPLIER;

        // Override with full speed if computed speed exceeds minimal threshold.
        if (this.speed > FULL_SPEED_THRESHOLD) {
            this.speed = 1.0D;
        }
    }

    @Override
    protected void onClear(LocalPlayer player) {
        this.speed = 0.0D;
    }

    @Override
    public boolean isActive(LocalPlayer p) {
        if (!isEnabled()) return false;
        if (MC.gameMode == null) return false;
        if (!ClientContext.properties.isMoveModifiersAllowed()) return false;
        if (p == null || !p.isAlive()) return false;

        return p.onGround() || (!p.isInWater() && !p.isInLava());
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}

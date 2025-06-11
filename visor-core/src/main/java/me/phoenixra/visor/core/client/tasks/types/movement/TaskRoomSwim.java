package me.phoenixra.visor.core.client.tasks.types.movement;

import lombok.Getter;

import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.PoseDataImpl;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomSwim extends VisorTask {
    private static final String ID = "room_swim";

    @Getter
    private static TaskRoomSwim instance;

    private static final double SWIM_SPEED = 1.3;
    private static final double FRICTION = 0.9;
    private static final double SWIM_MOTION_SCALE = 0.15;
    private static final double MIN_SWIM_THRESHOLD = 0.3;
    private static final double SPRINTING_THRESHOLD = 1.0;
    private static final double HEAD_PIVOT_Y_OFFSET = 0.3;

    private Vec3 motion = Vec3.ZERO;
    private double lastDist;

    public TaskRoomSwim(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {

        PoseDataImpl preTickPose = ClientContext.player
                .getPose(PoseType.PRE_TICK);
        final PoseElement mainHand = preTickPose.getController(ControllerHand.MAIN);
        final PoseElement offhand = preTickPose.getController(ControllerHand.OFFHAND);
        final PoseElement hmd = preTickPose.getHmd();

        final Vec3 mainHandPos = mainHand.getPosition();
        final Vec3 offhandPos = offhand.getPosition();

        final Vec3 betweenHandsPos = offhandPos
                .subtract(mainHandPos)
                .scale(0.5)
                .add(mainHandPos);
        final Vec3 headPivotPos = preTickPose.getHeadPivot()
                .subtract(0.0, HEAD_PIVOT_Y_OFFSET, 0.0);
        // Compute the direction from the head pivot to the midpoint, then blend with the HMD's direction.
        final Vec3 betweenHandsDir = betweenHandsPos
                .subtract(headPivotPos)
                .normalize()
                .add(hmd.getDirection())
                .scale(0.5);

        // Compute the aim vector from the main hand using a custom forward (-Z) vector.
        final Vec3 mainHandAim = mainHand.getCustomVector(new Vector3f(0.0f, 0.0f, -1.0f))
                .add(mainHand.getCustomVector(new Vector3f(0.0f, 0.0f, -1.0f)))
                .scale(0.5);

        final double swimPower = mainHandAim.add(betweenHandsDir).length() / 2.0;
        final double handDistance = headPivotPos.distanceTo(betweenHandsPos);
        final double distanceDelta = this.lastDist - handDistance;

        // If the hands moved closer together, compute a swim motion vector.
        if (distanceDelta > 0.0) {
            final Vec3 swimMotion = betweenHandsDir.scale(distanceDelta * SWIM_SPEED * swimPower);
            this.motion = this.motion.add(swimMotion.scale(SWIM_MOTION_SCALE));
        }

        this.lastDist = handDistance;

        final double motionLength = this.motion.length();
        player.setSwimming(motionLength > MIN_SWIM_THRESHOLD);
        player.setSprinting(motionLength > SPRINTING_THRESHOLD);
        player.push(this.motion.x, this.motion.y, this.motion.z);
        // Apply friction to gradually dampen the motion.
        this.motion = this.motion.scale(FRICTION);
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(LocalPlayer p) {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.MOVEMENT_MODIFIERS)){
            return false;
        }
        if (MC.screen != null) return false;
        if (MC.gameMode == null) return false;
        if (p == null || !p.isAlive()) return false;
        if (!p.isInWater() && !p.isInLava()) return false;
        if (p.zza > 0.0F) return false;
        if (p.xxa > 0.0F) return false;
        return true;
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

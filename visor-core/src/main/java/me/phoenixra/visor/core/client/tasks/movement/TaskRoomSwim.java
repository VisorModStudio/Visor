package me.phoenixra.visor.core.client.tasks.movement;

import lombok.Getter;

import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.PoseDataImpl;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomSwim extends VisorTask {
    private static final String ID = "room_swim";

    @Getter
    private static TaskRoomSwim instance;

    private static final float SWIM_SPEED = 1.3f;
    private static final float FRICTION = 0.9f;
    private static final float SWIM_MOTION_SCALE = 0.1f;
    private static final float MIN_SWIM_THRESHOLD = 0.3f;
    private static final float SPRINTING_THRESHOLD = 1.0f;
    private static final float HEAD_PIVOT_Y_OFFSET = 0.3f;

    private Vector3fc motion = new Vector3f();
    private float lastDist;

    public TaskRoomSwim(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {

        PoseDataImpl preTickPose = ClientContext.player
                .getPoseData(PoseDataType.PRE_TICK);
        final PoseElement mainHand = preTickPose.getController(ControllerHand.MAIN);
        final PoseElement offhand = preTickPose.getController(ControllerHand.OFFHAND);
        final PoseElement hmd = preTickPose.getHmd();

        final Vector3fc mainHandPos = mainHand.getPosition();
        final Vector3fc offhandPos = offhand.getPosition();

        final Vector3fc betweenHandsPos = offhandPos
                .sub(mainHandPos, new Vector3f())
                .mul(0.5f)
                .add(mainHandPos);
        final Vector3fc headPivotPos = preTickPose.getHeadPivot()
                .sub(0.0f, HEAD_PIVOT_Y_OFFSET, 0.0f, new Vector3f());
        // Compute the direction from the head pivot to the midpoint, then blend with the HMD's direction.
        final Vector3fc betweenHandsDir = betweenHandsPos
                .sub(headPivotPos, new Vector3f())
                .normalize()
                .add(hmd.getDirection())
                .mul(0.5f);

        // Compute the aim vector from the main hand using a custom forward (-Z) vector.
        final var mainHandAim = mainHand.getCustomVector(new Vector3f(0.0f, 0.0f, -1.0f))
                .add(mainHand.getCustomVector(new Vector3f(0.0f, 0.0f, -1.0f)))
                .mul(0.5f);

        final float swimPower = mainHandAim.add(betweenHandsDir).length() / 2.0f;
        final float handDistance = headPivotPos.distance(betweenHandsPos);
        final float distanceDelta = this.lastDist - handDistance;

        // If the hands moved closer together, compute a swim motion vector.
        if (distanceDelta > 0.0) {
            final Vector3f swimMotion = betweenHandsDir
                    .mul(distanceDelta * SWIM_SPEED * swimPower, new Vector3f());
            this.motion = this.motion.add(swimMotion.mul(SWIM_MOTION_SCALE), new Vector3f());
        }

        this.lastDist = handDistance;

        final double motionLength = this.motion.length();
        player.setSwimming(motionLength > MIN_SWIM_THRESHOLD);
        player.setSprinting(motionLength > SPRINTING_THRESHOLD);
        player.push(this.motion.x(), this.motion.y(), this.motion.z());
        // Apply friction to gradually dampen the motion.
        this.motion = this.motion.mul(FRICTION, new Vector3f());
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

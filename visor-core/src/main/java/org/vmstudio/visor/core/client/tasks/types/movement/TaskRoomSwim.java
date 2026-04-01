package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;

import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;

import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;

import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomSwim extends VisorTask {
    private static final String ID = "movement_room_swim";

    @Getter
    private static TaskRoomSwim instance;

    private static final float SWIM_SPEED = 1.3f;
    private static final float FRICTION = 0.9f;
    private static final float SWIM_MOTION_SCALE = 0.12f;
    private static final float WATER_SPEED_CAP = 0.10f;
    private static final float WATER_SWIMMING_SPEED_CAP = 0.13f;
    private static final float LAVA_SPEED_CAP = 0.02f;
    private static final float LAVA_SWIMMING_SPEED_CAP = 0.03f;
    private static final float MIN_SWIM_THRESHOLD = 0.075f;
    private static final float SPRINTING_THRESHOLD = 0.115f;

    private Vector3fc motion = new Vector3f();
    private float lastDist;


    public TaskRoomSwim(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {

        LocalPlayerPose preTickPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK);
        var mainHand = preTickPose.getHand(HandType.MAIN);
        var offhand = preTickPose.getHand(HandType.OFFHAND);

        var mainHandPos = mainHand.getPosition();

        var betweenHandsPos = offhand.getPosition()
                .sub(mainHandPos, new Vector3f()).mul(0.5f)
                .add(mainHandPos);
        var headPivotPos = preTickPose.getHeadPivot()
                .sub(0.0f, 0.3f, 0.0f, new Vector3f());

        final Vector3fc betweenHandsDir = betweenHandsPos
                .sub(headPivotPos, new Vector3f())
                .normalize()
                .add(preTickPose.getHmd().getDirection())
                .mul(0.5f);

        var mainHandAim = mainHand.getCustomVector(VRMathUtils.BACK_VECTOR)
                .add(mainHand.getCustomVector(VRMathUtils.BACK_VECTOR))
                .mul(0.5f);

        float swimPower = mainHandAim.add(betweenHandsDir).length() / 2.0f;
        float handDistance = headPivotPos.distance(betweenHandsPos);
        float distanceDelta = this.lastDist - handDistance;

        if (distanceDelta > 0.0) {
            Vector3f swimMotion = betweenHandsDir
                    .mul(distanceDelta * SWIM_SPEED * swimPower, new Vector3f());
            this.motion = this.motion.add(swimMotion.mul(SWIM_MOTION_SCALE), new Vector3f());
        }

        float maxSwimSpeed = getMaxSwimSpeed(player);
        float motionLength = this.motion.length();
        if (motionLength > maxSwimSpeed) {
            this.motion = this.motion.normalize(new Vector3f())
                .mul(maxSwimSpeed);
            motionLength = maxSwimSpeed;
        }

        this.lastDist = handDistance;

        player.setSwimming(motionLength > MIN_SWIM_THRESHOLD);
        player.setSprinting(motionLength > SPRINTING_THRESHOLD);
        player.push(this.motion.x(), this.motion.y(), this.motion.z());

        this.motion = this.motion.mul(FRICTION, new Vector3f());
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {
        this.motion = new Vector3f();
        this.lastDist = 0.0f;
    }


    @Override
    public boolean isActive(LocalPlayer p) {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.MOVEMENT_MODIFIERS)){
            return false;
        }
        if(!VRClientSettings.isRoomSwimEnabled()) return false;
        if (MC.screen != null) return false;
        if (MC.gameMode == null) return false;
        if (p == null || !p.isAlive()) return false;
        if (p.isPassenger()) return false;
        if (!p.isInWater() && !p.isInLava()) return false;
        if (p.zza != 0.0F || p.xxa != 0.0F) return false;

        BlockPos playerBlockPos = p.blockPosition();
        boolean hasFluidAtBody = p.level()
            .getFluidState(playerBlockPos)
            .is(FluidTags.WATER)
            || p.level().getFluidState(playerBlockPos).is(FluidTags.LAVA);
        boolean hasFluidAboveBody = p.level()
            .getFluidState(playerBlockPos.above())
            .is(FluidTags.WATER)
            || p.level().getFluidState(playerBlockPos.above()).is(FluidTags.LAVA);

        if (!hasFluidAtBody || !hasFluidAboveBody) return false;
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

    private float getMaxSwimSpeed(LocalPlayer player) {
        boolean swimmingPose = player.isSwimming() || player.getPose() == Pose.SWIMMING;
        if (player.isInLava()) {
            return swimmingPose ? LAVA_SWIMMING_SPEED_CAP : LAVA_SPEED_CAP;
        }
        return swimmingPose ? WATER_SWIMMING_SPEED_CAP : WATER_SPEED_CAP;
    }
}

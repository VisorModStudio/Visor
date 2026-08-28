package org.vmstudio.visor.core.client.tasks.types.movement.vehicle;

import lombok.Getter;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRLocalPlayerImpl;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomSneak;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskVehicle extends VisorTask {

    private static final String ID = "movement_vehicle";

    @Getter
    private static TaskVehicle instance;

    public Vec3 premountPosRoom = new Vec3(0.0D, 0.0D, 0.0D);


    private static final double MINECART_MOVEMENT_FLOOR = 0.001D;
    // seat the rider facing against the direction of travel
    private static final float MINECART_RIDER_YAW_OFFSET_DEG = 180.0F;

    public float vehicleRotationDeg = 0.0F;

    public int rotationCooldown = 0;
    private int minecartTimer = 0;
    public int dismountDelay = 0;

    public TaskVehicle(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        if (player == null) return;

        handleAutoDismount(player);
        tickTimers();

        final LocalPlayer mcPlayer = MC.player;
        if (mcPlayer == null) return;

        if (!mcPlayer.isPassenger() || this.rotationCooldown > 0) {
            this.minecartTimer = 3;
            if (mcPlayer.isPassenger()) {
                this.vehicleRotationDeg = mcPlayer.getVehicle().getYRot();
            }
            return;
        }

        final Entity vehicle = mcPlayer.getVehicle();
        if (!(vehicle instanceof Minecart minecart)) {
            return;
        }

        updateMinecartTimer(minecart);
        updateVehicleRotationForMinecart(minecart);
        applyVehicleRotationToVRPlayer(minecart);
    }

    @Override
    protected void onClear(LocalPlayer player) {
        this.minecartTimer = 3;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (MC.isPaused()) return false;
        if (player == null || MC.gameMode == null) return false;
        return player.isAlive();
    }

    public void onStartRiding(Entity vehicle) {
        VRLocalPlayerImpl localPlayer = ClientContext.localPlayer;
        LocalPlayerPose tickPose = localPlayer.getPoseData(PlayerPoseType.TICK);

        final Vector3fc headPivot = localPlayer
                .getPoseData(PlayerPoseType.ROOM)
                .getHeadPivot();
        premountPosRoom = new Vec3(headPivot.x(), 0.0D, headPivot.z());

        dismountDelay = 5;

        final float hmdYawDeg = tickPose.getHmd().getYawDegrees();
        final float vehicleYawDeg = vehicle.getYRot() % 360.0F;

        vehicleRotationDeg = (float) Math.toDegrees(localPlayer.getRotationY());
        rotationCooldown = 2;

        if (vehicle instanceof Minecart) {
            return;
        }

        final float deltaDeg = rotationDeltaDeg(vehicleYawDeg, hmdYawDeg);
        localPlayer.setRotationY(
                localPlayer.getRotationY() - (float) Math.toRadians(deltaDeg)
        );
    }

    public void onStopRiding() {
        TaskRoomSneak.getInstance().setSneakTimer(0);
    }

    private void handleAutoDismount(LocalPlayer player) {
        if(!VRClientSettings.isRoomDismountVehicleEnabled()) return;
        if (!canAutoDismount(player)) return;


        Vector3fc mountPos = player.getVehicle().position().toVector3f();
        Vector3fc headPivot = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK)
                .getHeadPivot();

        double dx = headPivot.x() - mountPos.x();
        double dz = headPivot.z() - mountPos.z();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 1.0 && TaskRoomSneak.getInstance().getSneakTimer() == 0) {
            TaskRoomSneak.getInstance().setSneakTimer(5);
        }
    }

    private void tickTimers() {
        if (dismountDelay > 0) {
            --dismountDelay;
        }
        if (rotationCooldown > 0) {
            --rotationCooldown;
        }
    }

    private void updateMinecartTimer(Minecart minecart) {
        if (shouldMinecartTurnView(minecart)) {
            if (minecartTimer > 0) {
                --minecartTimer;
            }
        } else {
            minecartTimer = 3;
        }
    }

    private void updateVehicleRotationForMinecart(Minecart minecart) {
        final float rotationTargetDeg = getMinecartRenderYawDeg(minecart);
        if (minecartTimer > 0) {
            vehicleRotationDeg = rotationTargetDeg;
        }
    }

    private void applyVehicleRotationToVRPlayer(Minecart minecart) {
        final Entity vehicle = minecart;

        final float rotationTargetDeg = getMinecartRenderYawDeg(minecart);
        float deltaDeg = rotationDeltaDeg(rotationTargetDeg, vehicleRotationDeg);

        final Vec3 deltaMovement = vehicle.getDeltaMovement();
        final double horizontalSpeed = new Vec3(deltaMovement.x, 0.0, deltaMovement.z).length();

        float maxStepDeg = 200.0F * (float) (horizontalSpeed * horizontalSpeed);
        maxStepDeg = Math.max(maxStepDeg, 10.0F);

        deltaDeg = Mth.clamp(deltaDeg, -maxStepDeg, maxStepDeg);

        VRLocalPlayerImpl localPlayer = ClientContext.localPlayer;
        float deltaRad = (float) Math.toRadians(deltaDeg);

        localPlayer.setRotationY(localPlayer.getRotationY() - deltaRad);

        vehicleRotationDeg = (vehicleRotationDeg + deltaDeg) % 360.0F;
        if (vehicleRotationDeg < 0.0F) {
            vehicleRotationDeg += 360.0F;
        }
    }

    private float getMinecartRenderYawDeg(Minecart minecart) {
        final Vec3 delta = minecartDelta(minecart);
        if (delta.length() <= MINECART_MOVEMENT_FLOOR) {
            return vehicleRotationDeg;
        }
        // Minecraft yaw of the travel direction: yaw 0 looks down +Z, and grows toward -X
        final float travelYawDeg = (float) Math.toDegrees(Mth.atan2(-delta.x, delta.z));
        return travelYawDeg - MINECART_RIDER_YAW_OFFSET_DEG;
    }

    private boolean shouldMinecartTurnView(Minecart minecart) {
        return minecartDelta(minecart).length() > MINECART_MOVEMENT_FLOOR;
    }

    private Vec3 minecartDelta(Minecart minecart) {
        return new Vec3(
                minecart.getX() - minecart.xOld,
                minecart.getY() - minecart.yOld,
                minecart.getZ() - minecart.zOld
        );
    }

    private float rotationDeltaDeg(float targetDeg, float currentDeg) {
        float delta = (targetDeg - currentDeg) % 360.0F;
        if (delta > 180.0F) delta -= 360.0F;
        if (delta < -180.0F) delta += 360.0F;
        return delta;
    }

    public boolean canAutoDismount(LocalPlayer player) {
        return player != null
                && player.zza == 0.0F
                && player.xxa == 0.0F
                && player.isPassenger()
                && dismountDelay == 0;
    }

    public static Vector3fc getVehicleLookDirection(LocalPlayer player) {
        if (player == null) return null;

        final Entity entity = player.getVehicle();
        if (entity instanceof AbstractHorse || entity instanceof Boat) {
            if (player.zza <= 0) return null;
            return ClientContext.localPlayer
                    .getRotationElement(PlayerPoseType.TICK)
                    .getDirection();
        }
        if (entity instanceof Mob mob && mob.isControlledByLocalInstance()) {
            final HandType handWithFood = ItemClassifier.FOOD_STICK
                    .is(player.getMainHandItem().getItem())
                    ? HandType.MAIN
                    : HandType.OFFHAND;
            final VRPose handPose = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.TICK)
                    .getHand(handWithFood);
            return handPose.getDirection().normalize(new Vector3f());
        }
        return null;
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
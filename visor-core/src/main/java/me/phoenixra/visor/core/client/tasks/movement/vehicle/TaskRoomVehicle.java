package me.phoenixra.visor.core.client.tasks.movement.vehicle;

import lombok.Getter;
import me.phoenixra.visor.api.common.player.PoseElement;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.compatibility.ItemClassifier;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.player.VRLocalPlayerImpl;
import me.phoenixra.visor.core.client.player.pose.LocalPlayerPose;
import me.phoenixra.visor.core.client.tasks.movement.TaskRoomSneakDis;
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

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomVehicle extends VisorTask {
    private static final String ID = "room_vehicle";

    @Getter
    private static TaskRoomVehicle instance;

    // Position in room coordinates before mounting.
    public Vec3 premountPosRoom = new Vec3(0.0D, 0.0D, 0.0D);
    public float vehicleRotation = 0.0F;
    public int rotationCooldown = 0;
    private int minecartTimer;
    public int dismountDelay = 0;

    public TaskRoomVehicle(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {

        if (canAutoDismount(player)) {
            Vector3fc mountPos = player.getVehicle().position().toVector3f();
            Vector3fc headPivot = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.TICK).getHeadPivot();
            double distance = Math.sqrt(
                    (headPivot.x() - mountPos.x())
                            * (headPivot.x() - mountPos.x())
                            + (headPivot.z() - mountPos.z())
                            * (headPivot.z() - mountPos.z())
            );

            if (distance > 0.7
                    && TaskRoomSneakDis.getInstance().getSneakTimer() == 0) {
                TaskRoomSneakDis.getInstance().setSneakTimer(5);
            }

        }

        // Decrement dismount and rotation cooldown timers
        if (this.dismountDelay > 0) {
            --this.dismountDelay;
        }
        if (this.rotationCooldown > 0) {
            --this.rotationCooldown;
        }

        final LocalPlayer mcPlayer = MC.player;
        // If player is not a passenger or if rotation is cooling down,
        // reset the minecart timer and update vehicleRotation from the current vehicle.
        if (!mcPlayer.isPassenger() || this.rotationCooldown != 0) {
            this.minecartTimer = 3;
            if (mcPlayer.isPassenger()) {
                this.vehicleRotation = mcPlayer.getVehicle().getYRot();
            }
            return;
        }

        final Entity vehicle = mcPlayer.getVehicle();
        // Only process further if the vehicle is a Minecart.
        if (!(vehicle instanceof Minecart)) {
            return;
        }
        final Minecart minecart = (Minecart) vehicle;

        // Update minecart timer based on whether the minecart is actively turning.
        if (shouldMinecartTurnView(minecart)) {
            if (this.minecartTimer > 0) {
                --this.minecartTimer;
            }
        } else {
            this.minecartTimer = 3;
        }

        // Compute target rotation based on minecart movement.
        final double rotationTarget = getMinecartRenderYaw(minecart);
        if (this.minecartTimer > 0) {
            this.vehicleRotation = (float) rotationTarget;
        }

        // Compute horizontal speed (ignoring Y axis)
        final Vec3 deltaMovement = vehicle.getDeltaMovement();
        final double horizontalSpeed = new Vec3(deltaMovement.x, 0.0, deltaMovement.z).length();

        // Calculate a "smoothed" value proportional to the square of the speed, clamped to a minimum.
        float smoothed = 200.0F * (float) (horizontalSpeed * horizontalSpeed);
        smoothed = Math.max(smoothed, 10.0F);

        VRLocalPlayerImpl vrClientPlayer = ClientContext.localPlayer;
        // Determine how much to rotate by comparing the target and current rotation.
        float rotateTo = rotationDelta((float) rotationTarget, this.vehicleRotation);
        // Clamp the rotation adjustment within [-smoothed, smoothed]
        smoothed = (float) Math.toRadians(smoothed);
        rotateTo = Math.min(smoothed, Math.max(rotateTo, -smoothed));

        // Apply the rotation adjustment
        vrClientPlayer.setRotationY(
                vrClientPlayer.getPoseData(PlayerPoseType.TICK).getRotationY() + rotateTo
        );
        // Update vehicle rotation and keep it within 0-360 degrees.
        this.vehicleRotation = (this.vehicleRotation - rotateTo) % 360.0F;
    }

    @Override
    protected void onClear(LocalPlayer player) {
        this.minecartTimer = 2;

    }

    @Override
    public boolean isActive(LocalPlayer p) {

        if (MC.isPaused()) {
            return false;
        }
        if (p == null || MC.gameMode == null) {
            return false;
        }
        return p.isAlive();
    }

    /**
     * Called when the player starts riding a vehicle.
     *
     * @param vehicle the vehicle being ridden.
     */
    public void onStartRiding(Entity vehicle) {
        VRLocalPlayerImpl vrClientPlayer = ClientContext.localPlayer;
        LocalPlayerPose preTickPose = vrClientPlayer
                .getPoseData(PlayerPoseType.TICK);

        final Vector3fc headPivot = vrClientPlayer
                .getPoseData(PlayerPoseType.ROOM)
                .getHeadPivot();
        // Record the player's room position (ignoring vertical component)
        this.premountPosRoom = new Vec3(headPivot.x(), 0.0D, headPivot.z());
        this.dismountDelay = 5;

        final float hmdYaw = preTickPose.getHmd().getRotationYCache();
        final float vehicleYRotation = vehicle.getYRot() % 360.0F;
        this.vehicleRotation = vrClientPlayer.getPoseData(PlayerPoseType.TICK).getRotationY();
        this.rotationCooldown = 2;

        // For Minecarts, no additional rotation adjustment is needed.
        if (vehicle instanceof Minecart) {
            return;
        }

        // Adjust rotation offset for other vehicles based on the difference between vehicle rotation and HMD yaw.
        final float rotationDelta = rotationDelta(vehicleYRotation, hmdYaw);
        vrClientPlayer.setRotationY(
                preTickPose.getRotationY() + rotationDelta
        );
    }

    /**
     * Called when the player stops riding.
     */
    public void onStopRiding() {
        TaskRoomSneakDis.getInstance().setSneakTimer(0);
    }

    /**
     * Computes the target render yaw for a Minecart.
     *
     * @param minecart the minecart entity.
     * @return the computed yaw.
     */
    private float getMinecartRenderYaw(Minecart minecart) {
        final Vec3 delta = new Vec3(
                minecart.getX() - minecart.xOld,
                minecart.getY() - minecart.yOld,
                minecart.getZ() - minecart.zOld
        );
        final float yaw = (float) Math.toDegrees(Mth.atan2(-delta.x, delta.z));
        // If the minecart is turning, adjust the yaw; otherwise, use the stored rotation.
        return shouldMinecartTurnView(minecart) ? -180.0F + yaw : this.vehicleRotation;
    }

    /**
     * Determines if the minecart is turning by checking if its movement delta is significant.
     *
     * @param minecart the minecart entity.
     * @return true if the minecart is actively turning.
     */
    private boolean shouldMinecartTurnView(Minecart minecart) {
        final Vec3 delta = new Vec3(
                minecart.getX() - minecart.xOld,
                minecart.getY() - minecart.yOld,
                minecart.getZ() - minecart.zOld
        );
        return delta.length() > 0.001D;
    }

    /**
     * Determines if auto-dismount conditions are met.
     *
     * @param player the local player.
     * @return true if auto-dismount is allowed.
     */
    public boolean canAutoDismount(LocalPlayer player) {
        return player.zza == 0.0F
                && player.xxa == 0.0F
                && player.isPassenger()
                && this.dismountDelay == 0;
    }

    /**
     * Computes the vehicle look direction for certain types of vehicles.
     *
     * @param player the local player.
     * @return the direction vector, or null if not applicable.
     */
    public static Vector3fc getVehicleLookDirection(LocalPlayer player) {
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
            final PoseElement handPose = ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.TICK)
                    .getHand(handWithFood);
            return handPose.getDirection().normalize(new Vector3f());
        }
        return null;
    }

    private float rotationDelta(float start, float end) {
        float radiansEnd = (float) Math.toRadians(end);
        float radiansStart = (float) Math.toRadians(start);
        return (float) Mth.atan2(Mth.sin(radiansEnd - radiansStart), Mth.cos(radiansEnd - radiansStart));
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

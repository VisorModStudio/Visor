package me.phoenixra.visor.core.client.tasks.types.game.movement;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.compatibility.ItemClassifier;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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
        if (MC.isPaused()) return;

        if (canAutoDismount(player)) {
            Vec3 mountPos = player.getVehicle().position();
            Vec3 headPivot = ClientContext.player
                    .getPose(PoseType.PRE_TICK).getHeadPivot();
            double distance = Math.sqrt(
                    (headPivot.x - mountPos.x)
                            * (headPivot.x - mountPos.x)
                            + (headPivot.z - mountPos.z)
                            * (headPivot.z - mountPos.z)
            );

            if (distance > 0.7D
                    && TaskRoomSneak.getInstance().getSneakTimer() == 0) {
                TaskRoomSneak.getInstance().setSneakTimer(5);
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

        VRClientPlayer vrClientPlayer = ClientContext.player;
        // Determine how much to rotate by comparing the target and current rotation.
        float rotateTo = rotationDelta((float) rotationTarget, this.vehicleRotation);
        // Clamp the rotation adjustment within [-smoothed, smoothed]
        smoothed = (float) Math.toRadians(smoothed);
        rotateTo = Math.min(smoothed, Math.max(rotateTo, -smoothed));

        // Apply the rotation adjustment
        vrClientPlayer.setRotationYaw(
                vrClientPlayer.getRotationYaw() + rotateTo
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
        if (!isEnabled() || p == null || MC.gameMode == null) {
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
        VRClientPlayer vrClientPlayer = ClientContext.player;
        PoseDataImpl preTickPose = vrClientPlayer
                .getPose(PoseType.PRE_TICK);

        final Vec3 headPivot = vrClientPlayer
                .getPose(PoseType.ROOM)
                .getHeadPivot();
        // Record the player's room position (ignoring vertical component)
        this.premountPosRoom = new Vec3(headPivot.x, 0.0D, headPivot.z);
        this.dismountDelay = 5;

        final float hmdYaw = preTickPose.getHmd().getYaw();
        final float vehicleYRotation = vehicle.getYRot() % 360.0F;
        this.vehicleRotation = vrClientPlayer.getRotationYaw();
        this.rotationCooldown = 2;

        // For Minecarts, no additional rotation adjustment is needed.
        if (vehicle instanceof Minecart) {
            return;
        }

        // Adjust rotation offset for other vehicles based on the difference between vehicle rotation and HMD yaw.
        final float rotationDelta = rotationDelta(vehicleYRotation, hmdYaw);
        vrClientPlayer.setRotationYaw(
                preTickPose.getRotationYaw() + rotationDelta
        );
    }

    /**
     * Called when the player stops riding.
     */
    public void onStopRiding() {
        TaskRoomSneak.getInstance().setSneakTimer(0);
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
    public static Vec3 getVehicleLookDirection(LocalPlayer player) {
        final Entity entity = player.getVehicle();
        if (entity instanceof AbstractHorse || entity instanceof Boat) {
            if (player.zza <= 0) return null;
            return ClientContext.player
                    .getRotationElement(PoseType.PRE_TICK)
                    .getDirection();
        }
        if (entity instanceof Mob mob && mob.isControlledByLocalInstance()) {
            final ControllerHand handWithFood = ItemClassifier.FOOD_STICK
                    .is(player.getMainHandItem().getItem())
                    ? ControllerHand.MAIN
                    : ControllerHand.OFFHAND;
            final PoseElement handPose = ClientContext.player
                    .getPose(PoseType.PRE_TICK)
                    .getController(handWithFood);
            return handPose.getDirection().normalize();
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

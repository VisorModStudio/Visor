package me.phoenixra.visor.core.client.tasks.types.movement.vehicle;


import lombok.Getter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomBoat extends VisorTask {
    public static final String ID = "room_boat";

    @Getter
    private static TaskRoomBoat instance;

    private static final float MAX_FORWARD_SPEED = 2.0F;
    private static final float MAX_ROTATION_SPEED = 1.0F;
    private static final double SPEED_OFFSET = 0.5;
    private static final float FORWARD_DISABLE_THRESHOLD = 0.2F;

    @Getter
    private float oarLeft;
    @Getter
    private float oarRight;
    @Getter
    private float moveForward;

    public TaskRoomBoat(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    public void onRun(LocalPlayer player) {

        final double handRightSpeed = ClientContext.rawPoseHandler.getControllerData(
                ControllerHand.MAIN
        ).getPositionHistory().averageSpeed(0.5f);
        final double handLeftSpeed = ClientContext.rawPoseHandler.getControllerData(
                ControllerHand.OFFHAND
        ).getPositionHistory().averageSpeed(0.5f);

        // Calculate oar speeds by subtracting an offset.
        this.oarRight = (float) Math.max(handRightSpeed - SPEED_OFFSET, 0.0);
        this.oarLeft = (float) Math.max(handLeftSpeed - SPEED_OFFSET, 0.0);

        // Only move forward if both oars are active.
        this.moveForward = (this.oarRight > 0.0F && this.oarLeft > 0.0F)
                ? (this.oarRight + this.oarLeft) / 2.0F : 0.0F;

        // Clamp moveForward to its maximum value.
        if (this.moveForward > MAX_FORWARD_SPEED) {
            this.moveForward = MAX_FORWARD_SPEED;
        }

        // Clamp oar speeds to maximum rotation speed.
        if (this.oarRight > MAX_ROTATION_SPEED) {
            this.oarRight = MAX_ROTATION_SPEED;
        }
        if (this.oarLeft > MAX_ROTATION_SPEED) {
            this.oarLeft = MAX_ROTATION_SPEED;
        }

        // If moving forward beyond a threshold, disable oar rotation.
        if (this.moveForward > FORWARD_DISABLE_THRESHOLD) {
            this.oarRight = 0.0F;
            this.oarLeft = 0.0F;
        }
    }

    @Override
    public void onClear(LocalPlayer player) {
        this.oarLeft = 0.0F;
        this.oarRight = 0.0F;
        this.moveForward = 0.0F;
    }

    @Override
    public boolean isActive(LocalPlayer p) {
        if (!isEnabled()) return false;
        if (p == null || !p.isAlive()) return false;
        if (MC.gameMode == null) return false;
        if (Minecraft.getInstance().options.keyUp.isDown()) return false;
        return p.getVehicle() instanceof Boat;
    }

    public boolean isRowing() {
        return this.oarRight + this.oarLeft + this.moveForward > 0.1F;
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

package org.vmstudio.visor.core.client.tasks.types.movement.vehicle;


import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import lombok.Getter;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.tasks.types.TaskRoomConsume;
import org.vmstudio.visor.core.client.utils.RoomTasksUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskBoat extends VisorTask {
    public static final String ID = "movement_boat";

    @Getter
    private static TaskBoat instance;

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

    public TaskBoat(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    public void onRun(LocalPlayer player) {

        final double handRightSpeed = rowingSpeed(player, HandType.MAIN);
        final double handLeftSpeed = rowingSpeed(player, HandType.OFFHAND);

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
        if (!VRClientSettings.isRoomBoatRowingEnabled()) return false;
        if (p == null || !p.isAlive()) return false;
        if (MC.gameMode == null) return false;
        if (McVersionClientUtils.screen() != null) return false;
        if (Minecraft.getInstance().options.keyUp.isDown()) return false;
        return McVersionUtils.isBoat(p.getVehicle());
    }

    private double rowingSpeed(LocalPlayer player, HandType hand) {
        if (ClientContext.cursorHandler.isHandFocused(hand)
                || (player.isUsingItem() && player.getUsedItemHand() == hand.asInteractionHand())) {
            return 0.0;
        }
        return ClientContext.rawPoseHandler.getControllerData(hand)
                .getPositionHistory().averageSpeed(0.5f);
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

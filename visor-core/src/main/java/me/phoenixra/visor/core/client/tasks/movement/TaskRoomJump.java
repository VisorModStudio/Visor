package me.phoenixra.visor.core.client.tasks.movement;

import lombok.Getter;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomJump extends VisorTask {
    private static final String ID = "movement_room_jump";

    @Getter
    private static TaskRoomJump instance;

    public TaskRoomJump(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        var historyRelative = ClientContext.localPlayer.getPoseHistoryRelative();

        double yDelta = historyRelative.headPivotNetMovement(5).y;
        if (yDelta < 0.1D) {
            return;
        }


        double fullHeight = ClientContext.localPlayer.getFullHeight();
        double actualHeight = ClientContext.localPlayer.getActualHeight();

        if (actualHeight / fullHeight >= VRClientSettings.getJumpThreshold()) {
            player.jumpFromGround();
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(LocalPlayer p) {

        if (p == null
                || !p.isAlive()
                || MC.gameMode == null) {
            return false;
        }
        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.MOVEMENT_MODIFIERS)) {
            return false;
        }
        // Only allow jump if the player is on solid ground
        // and not performing other actions.
        if (!p.isInWater() && !p.isInLava() && p.onGround()) {
            return !p.isShiftKeyDown() && !p.isPassenger();
        }
        return false;
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

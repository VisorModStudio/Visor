package me.phoenixra.visor.core.client.tasks.types.game.movement;

import lombok.Getter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.phoenixra.visor.core.client.VisorClientImpl.LOGGER;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomJump extends VisorTask {
    private static final String ID = "room_jump";

    @Getter
    private static TaskRoomJump instance;

    public TaskRoomJump(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        final var hmdData = ClientContext.rawPoseHandler.getHmdData();
        final var pivotHistory = hmdData.getPivotHistory();

        final double netMovementY = pivotHistory.netMovement(0.25D).y;
        if (netMovementY < 0.1D) {
            return;
        }


        final double latestY = pivotHistory.latest().y;
        final double playerHeight = VRClientSettings.getPlayerHeight();
        final double deltaY = latestY - playerHeight;

        if (deltaY > VRClientSettings.getJumpThreshold()) {
            player.jumpFromGround();
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(LocalPlayer p) {
        if (!isEnabled() || p == null || !p.isAlive() || MC.gameMode == null) {
            return false;
        }
        // Only active if movement is not blocked or falling is simulated.
        if (!ClientContext.properties.isMoveModifiersAllowed()) {
            return false;
        }
        // Only allow jump if the player is on solid ground and not performing other actions.
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

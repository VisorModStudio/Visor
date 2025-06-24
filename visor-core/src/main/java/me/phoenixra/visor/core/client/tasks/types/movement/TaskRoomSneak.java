package me.phoenixra.visor.core.client.tasks.types.movement;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomSneak extends VisorTask {
    private static final String ID = "room_sneak";

    @Getter
    private static TaskRoomSneak instance;
    @Getter
    private boolean sneaking = false;

    @Getter @Setter
    private int sneakTimer = 0;

    public TaskRoomSneak(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        if (!MC.isPaused() && sneakTimer > 0) {
            --sneakTimer;
        }

        final double playerHeight = VRClientSettings.getPlayerHeight();
        final double latestPivotY = ClientContext.rawPoseHandler.getHmdData()
                .getPivotHistory().latest().y();
        final double sneakThreshold = VRClientSettings.getSneakThreshold();

        // Determine if the difference between the configured height and the current head height exceeds the threshold.
        this.sneaking = (playerHeight - latestPivotY) > sneakThreshold;
    }

    @Override
    protected void onClear(LocalPlayer player) {
        this.sneaking = false;
    }

    @Override
    public boolean isActive(LocalPlayer p) {
        if(!ClientContext.visor.isFeatureEnabled(ClientFeature.MOVEMENT_MODIFIERS)){
            return false;
        }
        if (MC.gameMode == null) {
            return false;
        }
        if (p != null && p.isAlive() && p.onGround()) {
            return !p.isPassenger();
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

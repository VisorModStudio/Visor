package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;

import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.core.client.utils.RoomTasksUtils;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomSneak extends VisorTask {
    private static final String ID = "movement_room_sneak";

    @Getter
    private static TaskRoomSneak instance;
    @Getter
    private boolean sneaking = false;

    public TaskRoomSneak(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        double fullHeight = ClientContext.localPlayer.getFullHeight();
        double actualHeight = ClientContext.localPlayer.getActualHeight();

        this.sneaking = !TaskRoomCrawl.getInstance().isCrawling()
                && (actualHeight / fullHeight) <= VRClientSettings.getRoomSneakThreshold();
    }

    @Override
    protected void onClear(LocalPlayer player) {
        this.sneaking = false;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if(!VRClientSettings.isRoomSneakEnabled()) return false;
        if(ClientContext.visor
                .isFeatureDisabled(ClientFeature.MOVEMENT_MODIFIERS)){
            return false;
        }
        if (MC.gameMode == null || player == null) {
            return false;
        }
        return RoomTasksUtils.canSneakOrCrawl(player);
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

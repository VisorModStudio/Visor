package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.utils.RoomTasksUtils;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

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
        var historyRelative = ClientContext.localPlayer.getPoseHistoryRoom();

        double yDelta = historyRelative.headPivotNetMovement(5).y;
        if (yDelta < 0.1D) {
            return;
        }


        double fullHeight = ClientContext.localPlayer.getFullHeight();
        double actualHeight = ClientContext.localPlayer.getActualHeight();

        if (actualHeight / fullHeight >= VRClientSettings.getRoomJumpThreshold()) {
            player.jumpFromGround();
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(LocalPlayer p) {

        if (p == null
                || MC.gameMode == null) {
            return false;
        }
        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.MOVEMENT_MODIFIERS)) {
            return false;
        }
        if(!VRClientSettings.isRoomJumpEnabled()) return false;
        if (McVersionClientUtils.screen() != null) return false;
        if (!RoomTasksUtils.isBodyFree(p) || ClientContext.localPlayer.isClimbing()) return false;
        // Only allow jump if the player is on solid ground
        // and not performing other actions.
        return p.onGround() && !p.isShiftKeyDown() && !isInFluidTooDeeply(p);
    }

    private static boolean isInFluidTooDeeply(LocalPlayer p) {
        double limit = p.getFluidJumpThreshold();
        return p.getFluidHeight(FluidTags.WATER) > limit
                || p.getFluidHeight(FluidTags.LAVA) > limit;
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

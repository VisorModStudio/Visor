package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.network.toserver.vrstate.CrawlingPayloadToServer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RegisterVisorTask
public class TaskRoomCrawl extends VisorTask {
    private static final String ID = "movement_room_crawl";

    @Getter
    private static TaskRoomCrawl instance;


    @Getter
    private boolean crawling;
    @Getter
    private boolean swimPose;

    public TaskRoomCrawl(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    public void onRun(LocalPlayer player) {
        double fullHeight = ClientContext.localPlayer.getFullHeight();
        double actualHeight = ClientContext.localPlayer.getActualHeight();

        final boolean isCrawling = (actualHeight / fullHeight) <= VRClientSettings.getRoomCrawlThreshold();
        applyState(player, isCrawling);
    }

    @Override
    public void onClear(LocalPlayer player) {
        this.swimPose = false;
        applyState(player, false);
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        if(ClientContext.visor.isFeatureDisabled(ClientFeature.MOVEMENT_MODIFIERS)){
            return false;
        }
        if(!VRClientSettings.isRoomCrawlEnabled()) return false;
        if(!VRServerSettings.isRoomCrawlingSupported()) return false;
        return isEnabled()
                && player != null
                && player.isAlive()
                && !player.isSleeping()
                && !player.isSpectator()
                && !player.isPassenger();
    }


    private void applyState(LocalPlayer player, boolean newCrawling) {
        if (this.crawling != newCrawling) {
            if (newCrawling) {
                player.setPose(Pose.SWIMMING);
                this.swimPose = true;
            }
            this.crawling = newCrawling;
            ClientNetworking.sendVRPacket(new CrawlingPayloadToServer(this.crawling));
        }
        if (!this.crawling && player.getPose() != Pose.SWIMMING) {
            this.swimPose = false;
        }
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

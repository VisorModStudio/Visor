package me.phoenixra.visor.core.client.tasks.movement;

import lombok.Getter;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.network.toserver.vrstate.CrawlingPayloadToServer;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.network.ClientNetworking;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
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

        final boolean isCrawling = (actualHeight / fullHeight) <= VRClientSettings.getCrawlThreshold();
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

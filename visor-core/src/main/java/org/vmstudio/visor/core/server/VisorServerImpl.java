package org.vmstudio.visor.core.server;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorServer;
import org.vmstudio.visor.api.common.VRLogger;
import org.vmstudio.visor.api.server.events.ServerStartedVREvent;
import org.vmstudio.visor.api.server.events.ServerStoppedVREvent;
import org.vmstudio.visor.api.server.events.VisorPlayerJoinedVREvent;
import org.vmstudio.visor.api.server.events.VisorPlayerLeftVREvent;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.api.server.player.VisorServerPlayer;
import org.vmstudio.visor.core.common.ServerConfig;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;

import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.core.server.player.VisorServerPlayerImpl;

import java.util.*;

public class VisorServerImpl implements VisorServer {
    public static VisorServerImpl INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME+"-Server");

    private final Map<UUID, VisorServerPlayerImpl> visorPlayers = new HashMap<>();


    @Getter
    private ConfigManager configManager;

    private VisorServerImpl() {}

    public static void create(){
        INSTANCE = new VisorServerImpl();
        INSTANCE.init();
        LOGGER.info("VR Server Core initialized");
    }

    private void init(){
        VisorAPI.Instance.setServer(this);
        this.configManager = new AtumConfigManager(
                "visor_server",
                VisorAPI.CONFIG_PATH,
                new VRLogger(LOGGER),
                true
        );
        // init server config
        ServerConfig serverConfig = new ServerConfig();
        try {
            serverConfig.onServerInit();
        }catch (Throwable e){
            LoggerUtils.printError(e);
        }

        //init common stuff if on dedicated server
        if (ModLoader.get().isDedicatedServer()) {

            AddonManagerImpl addonManager = (AddonManagerImpl) VisorAPI.addonManager();
            addonManager.initialize(
                    List.of()
            );
        }

        VisorAPI.eventBus().callEvent(new ServerStartedVREvent(this));
    }

    public void tickVR() {

    }
    public void onServerStop(){
        visorPlayers.clear();

        VisorAPI.eventBus().callEvent(new ServerStoppedVREvent(this));

        VisorAPI.Instance.setServer(null);
        INSTANCE = null;
        LOGGER.info("VR Server Core cleared");
    }

    @Override
    public VisorServerPlayerImpl getVisorPlayer(@NotNull ServerPlayer player) {
        var out = visorPlayers.get(player.getUUID());
        if(out != null && out.getMcPlayer() != player){
            out.setMcPlayer(player);
        }
        return out;
    }

    @Override
    public @Nullable VisorServerPlayer getVisorPlayer(@NotNull UUID playerUuid) {
        return visorPlayers.get(playerUuid);
    }


    public @NotNull Collection<? extends VisorServerPlayer> getAllVisorPlayers(){
        return visorPlayers.values();
    }




    public void addVisorPlayer(VisorServerPlayerImpl visorPlayer) {
        visorPlayers.put(
                visorPlayer.getMcPlayer().getUUID(),
                visorPlayer
        );
        VisorAPI.eventBus().callEvent(new VisorPlayerJoinedVREvent(visorPlayer));
    }



    public void removePlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        var existing = visorPlayers.get(uuid);
        if (existing != null) {
            VisorAPI.eventBus().callEvent(new VisorPlayerLeftVREvent(existing));
        }
        visorPlayers.remove(uuid);
    }
    public void updateMcPlayer(ServerPlayer player) {

        var visorPlayer = visorPlayers.get(player.getUUID());
        if (visorPlayer != null) {
            visorPlayer.setMcPlayer(player);
        }
    }


    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }


}

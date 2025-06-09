package me.phoenixra.visor.core.server;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.VisorServer;
import me.phoenixra.visor.api.common.MCVRLogger;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import me.phoenixra.visor.core.common.addon.AddonManagerImpl;

import me.phoenixra.visor.core.common.addon.AddonCoreServer;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VisorServerImpl implements VisorServer {
    public static VisorServerImpl INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME+"-Server");


    @Getter
    private final Map<UUID, VRServerPlayer> playersWithVR = new HashMap<>();

    @Getter
    private ConfigManager configManager;

    public VisorServerImpl() {
        VisorAPI.Instance.setServer(this);
        INSTANCE = this;

        this.configManager = new AtumConfigManager(
                "visor_server",
                VisorAPI.CONFIG_PATH,
                new MCVRLogger(LOGGER),
                true
        );
        // init server config
        ServerConfig serverConfig = new ServerConfig();
        try {
            serverConfig.init();
        }catch (Throwable e){
            LoggerUtils.printError(e);
        }

        //init addons manager if on dedicated server
        if (ModLoader.get().isDedicatedServer()) {

            var addonManager = new AddonManagerImpl(LOGGER);
            addonManager.initialize(
                    new AddonCoreServer(),
                    List.of()
            );
        }

    }

    public void tickVR() {

    }
    public void onServerStop(){
        playersWithVR.clear();
        VisorAPI.Instance.setServer(null);
        INSTANCE = null;
        LOGGER.info("VR Server Core cleared");
    }

    @Override
    public VRServerPlayerImpl getVrPlayer(@NotNull ServerPlayer player) {
        VRServerPlayerImpl out = (VRServerPlayerImpl) playersWithVR.get(player.getUUID());
        if(out != null && out.getMcPlayer() != player){
            out.mcPlayer = player;
        }
        return out;
    }
    @Override
    public @NotNull Collection<VRServerPlayer> getVrPlayers(){
        return playersWithVR.values();
    }

    @Override
    public boolean isVRPlayer(@NotNull ServerPlayer player) {
        VRServerPlayer vrPlayer = getVrPlayer(player);
        if (vrPlayer == null) {
            return false;
        }
        return vrPlayer.isVr();
    }

    public void putVrPlayer(VRServerPlayerImpl player) {
        playersWithVR.put(player.mcPlayer.getUUID(), player);
    }

    public void updateVrPlayer(ServerPlayer player) {
        VRServerPlayerImpl vrServerPlayer = (VRServerPlayerImpl) playersWithVR.get(player.getUUID());
        if (vrServerPlayer != null) {
            vrServerPlayer.mcPlayer = player;
        }
    }


    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }
}

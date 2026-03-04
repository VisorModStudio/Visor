package org.vmstudio.visor.core.server;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorServer;
import org.vmstudio.visor.api.common.VRLogger;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.ServerConfig;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;

import org.vmstudio.visor.core.common.addon.CoreAddonServer;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;
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

    private AddonManagerImpl addonManager;

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

        //init addons manager if on dedicated server
        if (ModLoader.get().isDedicatedServer()) {

            addonManager = new AddonManagerImpl(LOGGER);
            addonManager.initialize(
                    new CoreAddonServer(),
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
            out.setMcPlayer(player);
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
        return vrPlayer.isVRActive();
    }

    public void putVrPlayer(VRServerPlayerImpl player) {
        playersWithVR.put(player.getMcPlayer().getUUID(), player);
    }

    public void updateVrPlayer(ServerPlayer player) {
        VRServerPlayerImpl vrServerPlayer = (VRServerPlayerImpl) playersWithVR.get(player.getUUID());
        if (vrServerPlayer != null) {
            vrServerPlayer.setMcPlayer(player);
        }
    }


    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }


}

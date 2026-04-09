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
import org.vmstudio.visor.core.server.player.VisorPacketReceiver;

import java.util.*;

public class VisorServerImpl implements VisorServer {
    public static VisorServerImpl INSTANCE;

    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME+"-Server");


    @Getter
    private final Map<UUID, VisorPacketReceiver> visorPacketReceivers = new HashMap<>();
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

        //init common stuff if on dedicated server
        if (ModLoader.get().isDedicatedServer()) {

            VisorAPI.Instance.setVrPlayerSupplier(
                    mcPlayer-> getVrPlayer((ServerPlayer) mcPlayer)
            );
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
        visorPacketReceivers.clear();
        playersWithVR.clear();
        VisorAPI.Instance.setServer(null);
        INSTANCE = null;
        LOGGER.info("VR Server Core cleared");
    }

    public VisorPacketReceiver getPacketReceiver(@NotNull ServerPlayer player) {
        VisorPacketReceiver out = visorPacketReceivers.get(player.getUUID());
        if(out != null && out.getMcPlayer() != player){
            out.setMcPlayer(player);
        }
        return out;
    }
    @Override
    public VRServerPlayerImpl getVrPlayer(@NotNull ServerPlayer player) {
        VRServerPlayerImpl out = (VRServerPlayerImpl) playersWithVR.get(player.getUUID());
        if(out != null && out.getMcPlayer() != player){
            out.setMcPlayer(player);
        }
        return out;
    }


    public @NotNull Collection<VisorPacketReceiver> getPacketReceiver(){
        return visorPacketReceivers.values();
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
        return true;
    }

    public void addPacketReceiver(VisorPacketReceiver packetReceiver) {
        visorPacketReceivers.put(
                packetReceiver.getMcPlayer().getUUID(),
                packetReceiver
        );
    }
    public void addVrPlayer(VRServerPlayerImpl player) {
        playersWithVR.put(player.getMcPlayer().getUUID(), player);
        visorPacketReceivers.put(player.getMcPlayer().getUUID(), player);
    }



    public void removePlayer(ServerPlayer player) {
        playersWithVR.remove(player.getUUID());
        visorPacketReceivers.remove(player.getUUID());
    }
    public void updateMcPlayer(ServerPlayer player) {
        VRServerPlayerImpl vrServerPlayer = (VRServerPlayerImpl) playersWithVR.get(player.getUUID());
        if (vrServerPlayer != null) {
            vrServerPlayer.setMcPlayer(player);
        }
        VisorPacketReceiver packetReceiver = visorPacketReceivers.get(player.getUUID());
        if (packetReceiver != null) {
            packetReceiver.setMcPlayer(player);
        }
    }


    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }


}

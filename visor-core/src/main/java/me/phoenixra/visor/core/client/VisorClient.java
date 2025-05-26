package me.phoenixra.visor.core.client;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import me.phoenixra.atumvr.api.VRProvider;
import me.phoenixra.atumvr.api.VRState;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.IVisorClient;
import me.phoenixra.visor.api.client.IClientPlayer;
import me.phoenixra.visor.api.client.render.context.PreRenderContext;
import me.phoenixra.visor.api.client.render.context.RenderContext;
import me.phoenixra.visor.api.common.MCVRLogger;
import me.phoenixra.visor.api.common.addon.VRElementRegistry;
import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.gui.VRGuiManager;
import me.phoenixra.visor.core.client.provider.openxr.XrVRProvider;
import me.phoenixra.visor.core.client.render.VisorRenderer;
import me.phoenixra.visor.core.client.render.gameview.VRGameViewHandler;
import me.phoenixra.visor.core.client.render.gameview.hand.VRHandRenderer;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.common.addon.AddonManager;
import me.phoenixra.visor.core.common.addon.VRAddonClientCore;

import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;



public class VisorClient implements IVisorClient {

    public static Minecraft MC;
    
    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME);


    @Getter
    private final VRProvider vrProvider;

    @Getter
    private final ConfigManager configManager;



    public VisorClient() {
        MC = Minecraft.getInstance();

        ClientContext.visor = this;

        vrProvider = new XrVRProvider(
                VisorAPI.MOD_NAME,
                new MCVRLogger(LOGGER)
        );

        //-------Configuration-------
        configManager = new AtumConfigManager(
                "visor_client",
                VisorAPI.CONFIG_PATH,
                vrProvider.getLogger(),
                true
        );
        configManager.setPlaceholderHandler(
                new AtumPlaceholderHandler(vrProvider.getLogger())
        );
        VRClientSettingsHandler.init();

        //-------Main client classes-------
        ClientContext.properties = new ClientProperties();
        ClientContext.gameViewHandler = new VRGameViewHandler();
        ClientContext.guiManager = new VRGuiManager();
        ClientContext.player = new VRClientPlayer();

        //-------Addons-------

        //Addon Registries
        var registries = new ArrayList<VRElementRegistry<?>>();
        registries.addAll(ClientContext.gameViewHandler.getElementRegistries());
        registries.addAll(ClientContext.guiManager.getElementRegistries());

        //Addon init
        var addonManager = new AddonManager(LOGGER);

        addonManager.initialize(
                new VRAddonClientCore(),
                registries
        );

    }



    protected void initializeVR() throws Throwable{


        vrProvider.initializeVR();


    }


    public void syncVRState(){
        vrProvider.syncState();
    }

    public void preTickVR(){

        ClientContext.properties.preTick();
        ClientContext.player.preTick();
    }

    public void tickVR(){

        ++VisorState.TICK_COUNT;

        ProfilerFiller profiler = MC.getProfiler();




        VRRemotePlayers.getInstance().tick();


        ClientContext.gameViewHandler.tick();


    }
    public void postTickVR(){

        ClientContext.player.postTick();
    }


    public void preRenderVR(PreRenderContext context){
        vrProvider.preRender(context);
        if (MC.gameRenderer != null
                && MC.gameRenderer.getMainCamera() != null
                && MC.level != null
                && MC.cameraEntity != null) {
            MC.gameRenderer.getMainCamera().setup(
                    MC.level,
                    MC.cameraEntity,
                    false, false,
                    context.partialTick()
            );
        }

        if(!(MC.screen instanceof OptionsScreen)
                && VRClientSettings.getEyeFovScaleCurrent() != VRClientSettings.getEyesFovScale()){
            VRClientSettings.setEyeFovScaleCurrent(
                    VRClientSettings.getEyesFovScale()
            );
        }

        ClientContext.properties.preRender();

        ClientContext.player
                .preRender(context.partialTick());
    }


    public void renderVR(RenderContext context){
        ClientContext.renderer
                .render(context);
    }



    public boolean isActive(){
        VRState state = vrProvider.getState();
        return state.isActive();
    }

    public boolean isFocused(){
        VRState state = vrProvider.getState();
        return state.isFocused();
    }



    @Override
    public @NotNull IClientPlayer getClientPlayer() {
        return ClientContext.player;
    }

    @Override
    public @NotNull VisorRenderer getVrRenderer() {
        return ClientContext.renderer;
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }


    @Override
    public @NotNull VRGameViewHandler getGameViewHandler() {
        return ClientContext.gameViewHandler;
    }

    @Override
    public @NotNull VRHandRenderer getHandsRenderer() {
        return ClientContext.handRenderer;
    }

    @Override
    public @NotNull VRGuiManager getGuiManager() {
        return ClientContext.guiManager;
    }

    @Override
    public @NotNull ClientProperties getProperties() {
        return ClientContext.properties;
    }

    protected void destroy(){
        try {
            vrProvider.destroy();
        } catch (Throwable throwable) {
            LoggerUtils.printError(throwable);
        }

        ClientContext.renderer = null;
    }

}

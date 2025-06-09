package me.phoenixra.visor.core.client;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import me.phoenixra.atumvr.api.VRProvider;
import me.phoenixra.atumvr.api.VRState;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.VisorClient;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.ClientPlayer;
import me.phoenixra.visor.api.client.input.InputManager;
import me.phoenixra.visor.api.client.render.context.PreRenderContext;
import me.phoenixra.visor.api.client.render.context.RenderContext;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.MCVRLogger;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.gui.GuiManagerImpl;
import me.phoenixra.visor.core.client.input.InputManagerImpl;
import me.phoenixra.visor.core.client.provider.openxr.XrVRProvider;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.decoration.DecoratorManagerImpl;
import me.phoenixra.visor.core.client.render.decoration.hand.VRHandRendererImpl;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.client.tasks.VisorTaskRegistry;
import me.phoenixra.visor.core.common.addon.AddonManagerImpl;
import me.phoenixra.visor.core.common.addon.AddonCoreClient;

import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;



@Getter
public class VisorClientImpl implements VisorClient {

    public static Minecraft MC;
    
    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME);


    private VRProvider vrProvider;

    private ConfigManager configManager;

    private VisorTaskRegistry taskRegistry;

    private ClientFeaturesToggle featuresToggle;

    public VisorClientImpl() {
        MC = Minecraft.getInstance();

    }

    protected void prepare(){
        vrProvider = new XrVRProvider(
                VisorAPI.MOD_NAME,
                new MCVRLogger(LOGGER)
        );

        featuresToggle = new ClientFeaturesToggle();

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
        ClientContext.inputManager = new InputManagerImpl();
        ClientContext.decoratorManager = new DecoratorManagerImpl();
        ClientContext.guiManager = new GuiManagerImpl();
        ClientContext.player = new VRClientPlayer();

        //-------Addons-------
        taskRegistry = new VisorTaskRegistry();

        //Addon Registries
        var registries = new ArrayList<VisorElementRegistry<?>>();
        registries.add(taskRegistry);
        registries.addAll(ClientContext.inputManager.getElementRegistries());
        registries.addAll(ClientContext.decoratorManager.getElementRegistries());
        registries.addAll(ClientContext.guiManager.getElementRegistries());

        //Addon init
        var addonManager = new AddonManagerImpl(LOGGER);

        addonManager.initialize(
                new AddonCoreClient(),
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
        featuresToggle.preTick();
        ClientContext.inputManager.preTick();

        var tasks = ClientContext.visor.getTaskRegistry().getPreTick();
        for (VisorTask task : tasks) {
            if (task.isActive(null)) {
                task.run(null);
            } else {
                task.clear(null);
            }
        }

        ClientContext.player.preTick();
    }

    public void tickVR(){

        ++VisorState.TICK_COUNT;



        VRRemotePlayers.getInstance().tick();


        ClientContext.decoratorManager.tick();


    }
    public void postTickVR(){

        ClientContext.player.postTick();
    }


    public void preRenderVR(PreRenderContext context){
        vrProvider.preRender(context);
        ClientContext.inputManager.update();

        if(!(MC.screen instanceof OptionsScreen)
                && VRClientSettings.getEyeFovScaleCurrent() != VRClientSettings.getEyesFovScale()){
            VRClientSettings.setEyeFovScaleCurrent(
                    VRClientSettings.getEyesFovScale()
            );
        }

        featuresToggle.preRender();

        var tasks = ClientContext.visor.getTaskRegistry().getPreRender();
        for (VisorTask task : tasks) {
            if (task.isActive(null)) {
                task.run(null);
            } else {
                task.clear(null);
            }
        }

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
    public @NotNull ClientPlayer getPlayer() {
        return ClientContext.player;
    }

    @Override
    public @NotNull VisorRendererBase getRenderer() {
        return ClientContext.renderer;
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public InputManager getInputManager() {
        return ClientContext.inputManager;
    }

    @Override
    public @NotNull DecoratorManagerImpl getDecoratorManager() {
        return ClientContext.decoratorManager;
    }

    @Override
    public @NotNull VRHandRendererImpl getHandsRenderer() {
        return ClientContext.handRenderer;
    }

    @Override
    public @NotNull GuiManagerImpl getGuiManager() {
        return ClientContext.guiManager;
    }

    @Override
    public boolean isFeatureEnabled(@NotNull ClientFeature feature) {
        return featuresToggle.isAllowed(feature);
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

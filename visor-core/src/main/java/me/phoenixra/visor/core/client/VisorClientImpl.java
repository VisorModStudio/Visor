package me.phoenixra.visor.core.client;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import me.phoenixra.atumvr.api.VRProvider;
import me.phoenixra.atumvr.api.VRState;
import me.phoenixra.atumvr.api.utils.GLUtils;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.VisorClient;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.VRClientPlayer;
import me.phoenixra.visor.api.client.input.InputManager;
import me.phoenixra.visor.api.client.render.context.PreRenderContext;
import me.phoenixra.visor.api.client.render.context.RenderContext;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.MCVRLogger;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.core.client.data.VRClientPlayerImpl;
import me.phoenixra.visor.core.client.gui.VRGuiManagerImpl;
import me.phoenixra.visor.core.client.input.InputManagerImpl;
import me.phoenixra.visor.core.client.provider.openxr.XrProvider;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.decoration.DecorationRendererImpl;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.client.tasks.VisorTaskRegistry;
import me.phoenixra.visor.core.common.addon.AddonManagerImpl;
import me.phoenixra.visor.core.common.addon.CoreAddonClient;

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


    @Getter
    private VRProvider vrProvider;

    private ConfigManager configManager;

    private VisorTaskRegistry taskRegistry;

    private ClientFeaturesToggle featuresToggle;

    public VisorClientImpl() {
        MC = Minecraft.getInstance();

    }

    protected void prepare(){
        vrProvider = new XrProvider(
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
        ClientContext.decorationRenderer = new DecorationRendererImpl();
        ClientContext.guiManager = new VRGuiManagerImpl();
        ClientContext.player = new VRClientPlayerImpl();

        //-------Addons-------
        taskRegistry = new VisorTaskRegistry();

        //Addon Registries
        var registries = new ArrayList<VisorRegistry<?>>();
        registries.add(taskRegistry);
        registries.addAll(ClientContext.inputManager.getElementRegistries());
        registries.addAll(ClientContext.decorationRenderer.getElementRegistries());
        registries.addAll(ClientContext.guiManager.getElementRegistries());

        //Addon init
        var addonManager = new AddonManagerImpl(LOGGER);

        addonManager.initialize(
                new CoreAddonClient(),
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
        try {
            featuresToggle.preTick();
            ClientContext.inputManager.preTick();

            var tasks = ClientContext.visor.getTaskRegistry().getPreTick();
            for (VisorTask task : tasks) {
                if (task.isEnabledAndActive(null)) {
                    task.run(null);
                } else {
                    task.clear(null);
                }
            }

            ClientContext.player.preTick();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }

    public void tickVR(){
        try {
            ++VisorState.TICK_COUNT;


            VRRemotePlayers.getInstance().tick();


            ClientContext.decorationRenderer.tick();

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }

    }
    public void postTickVR(){
        try {
            ClientContext.player.postTick();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }


    public void earlyPreRenderVR(PreRenderContext context){
        try {
            vrProvider.preRender(context);
            ClientContext.inputManager.update();
            ClientContext.player.earlyPreRender();

            if (!(MC.screen instanceof OptionsScreen)
                    && VRClientSettings.getEyeFovScaleCurrent() != VRClientSettings.getEyesFovScale()) {
                VRClientSettings.setEyeFovScaleCurrent(
                        VRClientSettings.getEyesFovScale()
                );
            }

            featuresToggle.preRender();


            var tasks = ClientContext.visor.getTaskRegistry().getPreRender();
            for (VisorTask task : tasks) {
                if (task.isEnabledAndActive(null)) {
                    task.run(null);
                } else {
                    task.clear(null);
                }
            }

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }


    public void preRenderVR(PreRenderContext context){
        try{
            ClientContext.player.preRender(context.partialTicks());

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }
    public void renderVR(RenderContext context){
        try {
            context.profiler().push("VR render");
            ClientContext.renderer
                    .render(context);
            context.profiler().pop();
            GLUtils.checkGLError("post VR render");
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
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
    public @NotNull VRClientPlayer getPlayer() {
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
    public @NotNull InputManager getInputManager() {
        return ClientContext.inputManager;
    }

    @Override
    public @NotNull DecorationRendererImpl getDecorationRenderer() {
        return ClientContext.decorationRenderer;
    }



    @Override
    public @NotNull VRGuiManagerImpl getGuiManager() {
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

    }

}

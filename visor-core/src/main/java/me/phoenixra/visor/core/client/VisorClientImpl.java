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
import me.phoenixra.visor.api.client.player.VRClientPlayer;
import me.phoenixra.visor.api.client.player.VRLocalPlayer;
import me.phoenixra.visor.api.client.input.VRInputManager;
import me.phoenixra.visor.core.client.player.VRClientPlayers;
import me.phoenixra.visor.core.client.render.context.PreRenderContext;
import me.phoenixra.visor.core.client.render.context.RenderContext;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.MCVRLogger;
import me.phoenixra.visor.api.common.addon.component.ComponentRegistry;
import me.phoenixra.visor.core.client.gui.VRGuiManagerImpl;
import me.phoenixra.visor.core.client.input.VRInputManagerImpl;
import me.phoenixra.visor.core.client.provider.openxr.XrProvider;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.decoration.DecorationRendererImpl;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.client.tasks.VisorTaskRegistry;
import me.phoenixra.visor.core.common.addon.AddonManagerImpl;
import me.phoenixra.visor.core.common.addon.CoreAddonClient;

import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;


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
        ClientContext.settingsHandler = new VRClientSettingsHandler();

        //-------Main client classes-------
        ClientContext.localPlayer = VRClientPlayers.getLocalPlayer();
        ClientContext.inputManager = new VRInputManagerImpl();
        ClientContext.decorationRenderer = new DecorationRendererImpl();
        ClientContext.guiManager = new VRGuiManagerImpl();

        //-------Addons-------
        taskRegistry = new VisorTaskRegistry();

        ClientContext.addonManager = new AddonManagerImpl(LOGGER);

        var registries = new ArrayList<ComponentRegistry<?>>();
        registries.add(taskRegistry);
        registries.addAll(ClientContext.inputManager.getComponentRegistries());
        registries.addAll(ClientContext.decorationRenderer.getComponentRegistries());
        registries.addAll(ClientContext.guiManager.getComponentRegistries());

        ClientContext.addonManager.initialize(
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


    public void onGameLoopStart(){
        try {
            vrProvider.startFrame();
            ClientContext.inputManager.update();
            VRClientPlayers.onGameLoopStart();

            if (!(MC.screen instanceof OptionsScreen)
                    && VRClientSettings.getEyeFovScaleCurrent() != VRClientSettings.getEyesFovScale()) {
                VRClientSettings.setEyeFovScaleCurrent(
                        VRClientSettings.getEyesFovScale()
                );
            }

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
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

            VRClientPlayers.preTick();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }

    public void tickVR(){
        try {
            ++VisorState.TICK_COUNT;


            VRClientPlayers.tick();


            ClientContext.decorationRenderer.tick();

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }

    }
    public void postTickVR(){
        try {
            VRClientPlayers.postTick();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }




    public void preRenderVR(PreRenderContext context){
        try{
            featuresToggle.preRender();
            
            VRClientPlayers.preRender(context.partialTicks());

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
    public @NotNull VRLocalPlayer getVRLocalPlayer() {
        return ClientContext.localPlayer;
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
    public @NotNull VRInputManager getInputManager() {
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

    @Override
    public @Nullable VRClientPlayer getVRPlayer(@NotNull UUID uuid) {
        return VRClientPlayers.getPlayer(uuid);
    }

    protected void destroy(){
        try {
            vrProvider.destroy();
        } catch (Throwable throwable) {
            LoggerUtils.printError(throwable);
        }

    }

}

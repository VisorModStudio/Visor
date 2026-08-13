package org.vmstudio.visor.core.client;

import lombok.Getter;
import me.phoenixra.atumconfig.api.ConfigManager;
import me.phoenixra.atumconfig.core.AtumConfigManager;
import me.phoenixra.atumconfig.core.AtumPlaceholderHandler;
import me.phoenixra.atumvr.api.AtumVRProvider;
import me.phoenixra.atumvr.api.AtumVRState;
import me.phoenixra.atumvr.api.utils.GLUtils;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorClient;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.render.RenderFrameStartedVREvent;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.input.action.VRActions;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.input.VRInputManager;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.compatibility.immediatelyfast.ImmediatelyFastCompatHelper;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.input.actions.*;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.core.client.network.ClientPacketHandler;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.player.VRLocalPlayerImpl;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.context.PreRenderContext;
import org.vmstudio.visor.core.client.render.context.RenderContext;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.VRLogger;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.gui.VRGuiManagerImpl;
import org.vmstudio.visor.core.client.input.VRInputManagerImpl;
import org.vmstudio.visor.core.client.provider.openxr.XrProvider;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import org.vmstudio.visor.core.client.render.decoration.DecorationRendererImpl;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VRClientSettingsManager;
import org.vmstudio.visor.core.client.tasks.VisorTaskRegistry;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;
import org.vmstudio.visor.core.common.addon.CoreAddonClient;

import org.vmstudio.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.common.player.VRPoseImpl;

import java.util.ArrayList;
import java.util.UUID;


@Getter
public class VisorClientImpl implements VisorClient {

    public static Minecraft MC;
    
    public static final Logger LOGGER = LogManager.getLogger(VisorAPI.MOD_NAME);


    private AtumVRProvider vrProvider;

    private ConfigManager configManager;

    private VisorTaskRegistry taskRegistry;

    private ClientFeaturesToggle featuresToggle;


    @Getter
    private float partialTicks;

    public VisorClientImpl() {
        MC = Minecraft.getInstance();

    }

    protected void prepare(){
        vrProvider = new XrProvider(
                VisorAPI.MOD_NAME,
                new VRLogger(LOGGER)
        );

        featuresToggle = new ClientFeaturesToggle();

        VRPose.Instance.supplier = VRPoseImpl::new;

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
        ClientContext.settingsManager = new VRClientSettingsManager();

        //-------Main client classes-------
        ClientContext.localPlayer = new VRLocalPlayerImpl();
        ClientContext.inputManager = new VRInputManagerImpl();
        ClientContext.decorationRenderer = new DecorationRendererImpl();
        ClientContext.guiManager = new VRGuiManagerImpl();

        //-------API accessible VR actions-------
        VRActions.Provider.setMouseLeftMain(
                (actionSet -> new ActionLeftMouse(actionSet, HandType.MAIN))
        );
        VRActions.Provider.setMouseLeftOffhand(
                (actionSet -> new ActionLeftMouse(actionSet, HandType.OFFHAND))
        );

        VRActions.Provider.setMouseRightMain(
                (actionSet -> new ActionRightMouse(actionSet, HandType.MAIN))
        );
        VRActions.Provider.setMouseRightOffhand(
                (actionSet -> new ActionRightMouse(actionSet, HandType.OFFHAND))
        );

        VRActions.Provider.setMouseMiddleMain(
                (actionSet -> new ActionMiddleMouse(actionSet, HandType.MAIN))
        );
        VRActions.Provider.setMouseMiddleOffhand(
                (actionSet -> new ActionMiddleMouse(actionSet, HandType.OFFHAND))
        );

        VRActions.Provider.setMouseScrollMain(
                (actionSet -> new ActionScrollMouse(actionSet, HandType.MAIN))
        );
        VRActions.Provider.setMouseScrollOffhand(
                (actionSet -> new ActionScrollMouse(actionSet, HandType.OFFHAND))
        );

        VRActions.Provider.setShift(ActionShift::new);
        VRActions.Provider.setMenu(ActionMenu::new);
        VRActions.Provider.setScreenshot(ActionScreenshot::new);

        //-------Addons-------
        taskRegistry = new VisorTaskRegistry();


        var registries = new ArrayList<ComponentRegistry<?>>();
        registries.add(taskRegistry);
        registries.add(ClientContext.settingsManager.getPresetsRegistry());
        registries.addAll(ClientContext.inputManager.getComponentRegistries());
        registries.addAll(ClientContext.decorationRenderer.getComponentRegistries());
        registries.addAll(ClientContext.guiManager.getComponentRegistries());

        ClientContext.addonManager.initialize(
                registries
        );


        ClientContext.settingsManager.getPresetsCatalog().reload();


        var bodyType = ClientContext.decorationRenderer.getVrBodyTypeRegistry()
                .getComponent(VRClientSettings.getDefaultVrBody());
        if(bodyType == null){
            bodyType = VRBodyType.FALLBACK_BODY_TYPE;
        }
        ClientContext.localPlayer.setBodyType(bodyType);
        var delayedBodyInit = VisorState.getDelayedVrBodyInit();
        if(delayedBodyInit != null){
            ClientContext.decorationRenderer.getVrBodyTypeRegistry().getAllComponents()
                    .forEach(it->it.getRenderer().initModels(delayedBodyInit));
        }


        ImmPortalsCompatHelper.prepare(ClientContext.coreAddon);
        IrisCompatHelper.prepare(ClientContext.coreAddon);
        ImmediatelyFastCompatHelper.prepare(ClientContext.coreAddon);

    }




    protected void initializeVR() throws Throwable{


        vrProvider.initializeVR();


    }


    public void syncVRState(){
        vrProvider.syncState();
    }

    //When VR runtime asks app to become idle and don't send swapChains
    public void idleVRFrame(){
        try {
            vrProvider.idleFrame();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }


    public void onGameLoopStart(){
        try {
            //NON-VR + VR
            ++VisorState.FRAME_COUNT;

            if(VisorState.get().isNotActive()){
                return;
            }

            //VR ONLY
            VRRenderState.updateSceneType();
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
            if(VisorState.get().isNotActive()){
                //NON-VR ONLY
                VRClientPlayers.preTickRemote();
                return;
            }

            //VR ONLY
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
            //NON-VR + VR
            ++VisorState.TICK_COUNT;

            if(VisorState.get().isNotActive()){
                //NON-VR ONLY
                VRClientPlayers.tick();
                return;
            }

            //VR ONLY
            VRClientPlayers.tick();
            ClientContext.decorationRenderer.tick();

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }

    }
    public void postTickVR(){
        try {
            if(VisorState.get().isNotActive()){
                //NON-VR ONLY
                VRClientPlayers.postTickRemote();
                return;
            }
            //VR ONLY
            VRClientPlayers.postTick();
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }




    public void preRenderVR(PreRenderContext context){
        try{
            if(VisorState.get().isNotActive()){
                //NON-VR ONLY
                VRClientPlayers.preRenderRemote(context.partialTicks());
                return;
            }
            //VR ONLY
            partialTicks = context.partialTicks();

            featuresToggle.preRender();
            
            VRClientPlayers.preRender(context.partialTicks());

            var tasks = ClientContext.visor.getTaskRegistry().getPreRender();
            for (VisorTask task : tasks) {
                if (task.isEnabledAndActive(MC.player)) {
                    task.run(MC.player);
                } else {
                    task.clear(MC.player);
                }
            }
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }
    public void renderVR(RenderContext context){
        try {
            if(VisorState.get().isNotActive()){
                //NON-VR ONLY
                return;
            }
            //VR ONLY
            context.profiler().push("VR render");
            partialTicks = context.partialTicks();
            VisorAPI.eventBus().callEvent(
                    new RenderFrameStartedVREvent(
                            partialTicks
                    )
            );
            ClientContext.renderer.render(context);
            context.profiler().pop();
            GLUtils.checkGLError("post VR render");
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }



    public boolean isActive(){
        AtumVRState state = vrProvider.getState();
        return state.isActive();
    }

    public boolean isFocused(){
        AtumVRState state = vrProvider.getState();
        return state.isFocused();
    }



    @Override
    public @NotNull VRLocalPlayer getVRLocalPlayer() {
        return ClientContext.localPlayer;
    }

    @Override
    public @NotNull VRRendererBase getRenderer() {
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
    public boolean isInVisorServer() {
        return ClientNetworking.isServerSupportsVisor();
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

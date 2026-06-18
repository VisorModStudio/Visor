package org.vmstudio.visor.core.client;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.vmstudio.visor.api.VisorClientState;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.VRStateMode;
import org.vmstudio.visor.api.client.events.SessionStateChangedVREvent;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.VRSceneType;

import org.vmstudio.visor.core.client.gui.screens.VRPauseMenuScreen;
import org.vmstudio.visor.core.client.gui.screens.VRErrorReportScreen;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.vmstudio.visor.core.client.utils.ClientUtils;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VisorState implements VisorClientState {


    private static VRStateMode state = VRStateMode.OFF;


    public static int TICK_COUNT;

    public static long FRAME_COUNT;


    @Setter
    private static boolean minecraftLoaded = false;

    private static Runnable delayedErrorHandling = null;
    @Getter
    private static boolean vrInitFailed = false;

    @Getter @Setter
    private static EntityRendererProvider.Context delayedVrBodyInit = null;

    public static VRStateMode get(){
        return state;
    }

    public static void updateState() {

        //STARTUP (intended to be called only once)
        if (ClientContext.visor == null) {
            if (!minecraftLoaded) {
                return;
            }
            startClient();
        }

        //HANDLE DELAYED ERROR IN WORLD
        if(delayedErrorHandling != null
                && (MC.screen instanceof DisconnectedScreen
                || MC.screen instanceof TitleScreen)){
            delayedErrorHandling.run();
            delayedErrorHandling = null;
        }
        if(MC.level != null){
            //clean message
            vrInitFailed = false;
        }

        //INIT & DESTROY
        boolean canInit = VRClientSettings.getVrPlayMode().canInitVR();
        if (canInit) {
            if (state.isNotInitialized()) {
                initVR();
                return;
            }
        } else if (state.isInitialized()) {
            destroyVR();
            return;
        }

        if (state.isNotInitialized()) {
            return;
        }

        //ACTIVE & FOCUSED
        ClientContext.visor.syncVRState();


        var playMode = VRClientSettings.getVrPlayMode();
        boolean vrActive = playMode.canPlayVR()
                && (ClientContext.visor.isActive()
                || playMode == VRPlayMode.ALWAYS_ACTIVE);


        boolean changed = updateActive(vrActive);
        if(changed && !(playMode == VRPlayMode.WORLD_ONLY && state.isActive())){
            ClientUtils.disconnect("VR state changed");
            return;
        }

        if (state.isActive()) {
            if (ClientContext.visor.isFocused()) {
                setState(VRStateMode.FOCUSED);
            } else {
                if (state != VRStateMode.ACTIVE) {
                    if (MC.level != null) {
                        MC.setScreen(new VRPauseMenuScreen());
                    }
                }
                setState(VRStateMode.ACTIVE);
            }
        }


    }

    private static void startClient() {
        try {
            if (ClientContext.visor != null) {
                return;
            }

            VisorClientImpl.LOGGER.info("Starting Visor client...");

            VisorAPI.Instance.setClientState(new VisorState());

            ClientContext.visor = new VisorClientImpl();
            VisorAPI.Instance.setClient(
                    ClientContext.visor
            );
            ClientContext.visor.prepare();


            VisorClientImpl.LOGGER.info(
                    "Current VR Play Mode: {}",
                    VRClientSettings.getVrPlayMode()
            );
        } catch (Throwable e) {
            destroyVRWithErrorScreen(e);
        }

    }

    private static void initVR() {
        try {
            VisorClientImpl.LOGGER.info("Initializing VR session...");

            ClientContext.visor.initializeVR();
            VRRenderState.startVanillaPhase();

            setState(VRStateMode.INITIALIZED);
            vrInitFailed = false;

            VisorClientImpl.LOGGER.info("VR session INIT SUCCESS");
            LoggerUtils.sendPcInfo();
        } catch (Throwable e) {
            initFailed(e);
        }
    }

    //Has to be stable, on error MC will be crashed
    private static boolean updateActive(boolean active) {
        if (state.isActive() == active) {
            return false;
        }

        if (active) {
            activate();
        } else {
            deactivate();
        }


        if (!MC.getSoundManager()
                .getAvailableSounds().isEmpty()) {
            MC.getSoundManager().reload();
        }

        MC.resizeDisplay();
        MC.getWindow().updateVsync(MC.options.enableVsync().get());
        ClientContext.renderer.prepareReinit("Switched state");
        return true;
    }


    private static void activate() {
        setState(VRStateMode.ACTIVE);

        if (MC.player != null) {
            ClientContext.localPlayer.recenterOrigin(
                    MC.player, false
            );
        }
        // release mouse when switching to standing

        InputConstants.grabOrReleaseMouse(
                MC.getWindow().getWindow(),
                GLFW.GLFW_CURSOR_NORMAL,
                MC.mouseHandler.xpos(),
                MC.mouseHandler.ypos()
        );
    }

    private static void deactivate() {
        setState(VRStateMode.INITIALIZED);
        VRRenderState.startVanillaPhase();

        if (MC.gameRenderer != null) {
            MC.gameRenderer.checkEntityPostEffect(
                    MC.options.getCameraType().isFirstPerson()
                            ? MC.getCameraEntity() : null
            );
        }
        // grab/release mouse
        if (MC.screen != null || MC.level == null) {
            MC.mouseHandler.releaseMouse();
            InputConstants.grabOrReleaseMouse(
                    MC.getWindow().getWindow(),
                    GLFW.GLFW_CURSOR_NORMAL,
                    MC.mouseHandler.xpos(),
                    MC.mouseHandler.ypos()
            );
        } else {
            MC.mouseHandler.grabMouse();
            InputConstants.grabOrReleaseMouse(
                    MC.getWindow().getWindow(),
                    GLFW.GLFW_CURSOR_DISABLED,
                    MC.mouseHandler.xpos(),
                    MC.mouseHandler.ypos()
            );
        }
    }

    public static void destroyVRWithErrorScreen(Throwable throwable) {
        LoggerUtils.printError(throwable);

        destroyVR();

        VRClientSettings.setVrPlayMode(VRPlayMode.DISABLED);

        if(MC.level != null) {
            MC.level.disconnect();
        }
        delayedErrorHandling = ()-> VRErrorReportScreen.catchError(throwable,true);
    }
    private static void initFailed(Throwable throwable){
        destroyVR();

        VRClientSettings.setVrPlayMode(VRPlayMode.DISABLED);
        ClientContext.settingsManager.saveOptions();

        vrInitFailed = true;
        LoggerUtils.printError(throwable);
    }

    public static void destroyVR() {


        if (ClientContext.visor != null) {
            updateActive(false);
            setState(VRStateMode.OFF);
            ClientContext.visor.destroy();
        }else{
            setState(VRStateMode.OFF);
        }

    }
    public static void clearVrInitFailed() {
        vrInitFailed = false;
    }

    @Override
    public @NotNull VRStateMode stateMode() {
        return state;
    }

    @Override
    public @NotNull VRPlayMode playMode() {
        return VRClientSettings.getVrPlayMode();
    }

    @Override
    public @NotNull RenderPhase renderPhase() {
        return VRRenderState.getPhase();
    }

    @Override
    public VRRenderPass renderPass() {
        return VRRenderState.getRenderPass();
    }

    @Override
    public @NotNull VRSceneType sceneType() {
        return VRRenderState.getSceneType();
    }

    private static void setState(@NotNull VRStateMode newState) {
        if (state == newState) {
            return;
        }
        VRStateMode previousState = state;
        state = newState;
        VisorAPI.eventBus().callEvent(
                new SessionStateChangedVREvent(previousState, newState)
        );
    }
}

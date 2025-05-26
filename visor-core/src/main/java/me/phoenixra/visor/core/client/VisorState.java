package me.phoenixra.visor.core.client;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.IVisorState;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.network.toserver.vrstate.VRActivePayloadToServer;

import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.common.network.client.ClientNetworking;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import me.phoenixra.visor.core.common.utils.LoggerUtils;
import org.lwjgl.glfw.GLFW;

import static me.phoenixra.visor.core.client.VisorClient.MC;

public class VisorState implements IVisorState {



    @Getter
    private static VRStateMode stateMode = VRStateMode.DISABLED;


    public static int TICK_COUNT;

    public static long FRAME_COUNT;



    @Setter
    private static boolean minecraftLoaded = false;


    public static void updateState() {

        //STARTUP
        if (ClientContext.visor == null) {
            if (!minecraftLoaded) {
                return;
            }
            startClient();
        }

        //INIT & DESTROY
        boolean canInit = VRClientSettings.getVrPlayMode().canInitVR();
        if (canInit) {
            if (stateMode.isNotInitialized()) {
                initVR();
            }
        } else if (stateMode.isInitialized()) {
            destroyVR();
        }

        if (stateMode.isNotInitialized()) {
            return;
        }

        //ACTIVE & FOCUSED
        ClientContext.visor.syncVRState();

        boolean vrActive = ClientContext.visor.isActive()
                && VRClientSettings.getVrPlayMode().canPlayVR();

        updateActive(vrActive);

        if(stateMode.isActive()){
            if(ClientContext.visor.isFocused()){
                stateMode = VRStateMode.FOCUSED;
            }else{
                stateMode = VRStateMode.ACTIVE;
            }
        }

    }

    private static void startClient() {
        if (ClientContext.visor != null) {
            return;
        }

        VisorClient.LOGGER.info("Starting Visor client...");

        VisorAPI.Instance.setClientState(new VisorState());

        VisorAPI.Instance.setClient(new VisorClient());

        VisorClient.LOGGER.info(
                "Current VR Play Mode: {}",
                VRClientSettings.getVrPlayMode()
        );

    }

    private static void initVR(){
        try {
            VisorClient.LOGGER.info("Initializing VR session...");

            ClientContext.visor.initializeVR();
            VRRenderState.startVanillaPhase();

            stateMode = VRStateMode.INITIALIZED;

            VisorClient.LOGGER.info("VR session INIT SUCCESS");
            LoggerUtils.sendPcInfo();
        } catch (Throwable e) {
            destroyVRWithError(e);
        }
    }


    private static void updateActive(boolean active) {

        if(stateMode.isActive() == active){
            return;
        }

        if (active) {
            activate();
        } else {
            deactivate();
        }

        ClientNetworking.sendVRPacket(
                new VRActivePayloadToServer(
                        stateMode.isActive()
                )
        );

        if (!MC.getSoundManager()
                .getAvailableSounds().isEmpty()) {
            MC.getSoundManager().reload();
        }

        MC.resizeDisplay();
        MC.getWindow().updateVsync(MC.options.enableVsync().get());
        ClientContext.renderer.prepareReinit("Switched state");
    }


    private static void activate() {
        stateMode = VRStateMode.ACTIVE;

        if (MC.player != null) {
            ClientContext.player.recenterOrigin(
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
        stateMode = VRStateMode.INITIALIZED;

        if (MC.player != null) {
            VRRemotePlayers.getInstance().removePlayer(
                    MC.player.getUUID()
            );
        }
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

    public static void destroyVRWithError(Throwable throwable) {
        destroyVR();
        LoggerUtils.printError(throwable);

        VRClientSettings.setVrPlayMode(VRPlayMode.DISABLED);
    }

    public static void destroyVR() {

        if (ClientContext.visor != null) {
            updateActive(false);
            ClientContext.visor.destroy();
        }

        stateMode = VRStateMode.DISABLED;
    }


    @Override
    public VRStateMode stateMode() {
        return stateMode;
    }

    @Override
    public VRPlayMode playMode() {
        return VRClientSettings.getVrPlayMode();
    }

    @Override
    public RenderPhase renderPhase() {
        return VRRenderState.getCurrentPhase();
    }

    @Override
    public VRDisplay renderingDisplay() {
        return VRRenderState.getCurrentVRDisplay();
    }
}

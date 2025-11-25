package me.phoenixra.visor.core.client;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorClientState;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.common.network.toserver.vrstate.VRActivePayloadToServer;

import me.phoenixra.visor.core.client.gui.screens.GameMenuScreen;
import me.phoenixra.visor.core.client.gui.screens.VRErrorReportScreen;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.network.ClientNetworking;
import me.phoenixra.visor.api.common.utils.LoggerUtils;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VisorState implements VisorClientState {


    @Getter
    private static VRStateMode state = VRStateMode.OFF;


    public static int TICK_COUNT;

    public static long FRAME_COUNT;


    @Setter
    private static boolean minecraftLoaded = false;

    private static Runnable delayedErrorHandling = null;

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
        if(changed){
            return;
        }

        if (state.isActive()) {
            if (ClientContext.visor.isFocused()) {
                state = VRStateMode.FOCUSED;
            } else {
                if (state != VRStateMode.ACTIVE) {
                    if (MC.level != null) {
                        MC.setScreen(new GameMenuScreen());
                    }
                }
                state = VRStateMode.ACTIVE;
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

            state = VRStateMode.INITIALIZED;

            VisorClientImpl.LOGGER.info("VR session INIT SUCCESS");
            LoggerUtils.sendPcInfo();
        } catch (Throwable e) {
            destroyVRWithErrorScreen(e);
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

        ClientNetworking.sendVRPacket(
                new VRActivePayloadToServer(
                        state.isActive()
                )
        );

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
        state = VRStateMode.ACTIVE;

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
        state = VRStateMode.INITIALIZED;

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

        destroyVR();

        VRClientSettings.setVrPlayMode(VRPlayMode.DISABLED);

        if(MC.level != null) {
            MC.level.disconnect();
            delayedErrorHandling = ()-> VRErrorReportScreen.catchError(throwable,true);
        }else {
            VRErrorReportScreen.catchError(throwable, true);
        }
    }

    public static void destroyVR() {


        if (ClientContext.visor != null) {
            updateActive(false);
            state = VRStateMode.OFF;
            ClientContext.visor.destroy();
        }else{
            state = VRStateMode.OFF;
        }

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
    public VRCameraType renderCameraType() {
        return VRRenderState.getCameraType();
    }
}

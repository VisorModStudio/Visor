package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
//? if >=1.20.5 {
import net.minecraft.client.gui.screens.GenericMessageScreen;
//?} else {
/*import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
*///?}
//? if <1.21.9 {
/*import net.minecraft.client.gui.screens.ReceivingLevelScreen;
*///?}
//? if >=1.21.2 {
import net.minecraft.util.profiling.Profiler;
//?}

/**
 * Cross-mc-version Utils for client methods
 *
 */
public class McVersionClientUtils {

    public static long windowHandle() {
        //? if >=1.21.9 {
        return Minecraft.getInstance().getWindow().handle();
        //?} else {
        /*return Minecraft.getInstance().getWindow().getWindow();
        *///?}
    }

    public static Screen chatScreen(String initial) {
        //? if >=1.21.9 {
        return new ChatScreen(initial, false);
        //?} else {
        /*return new ChatScreen(initial);
        *///?}
    }
    private McVersionClientUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- LEVEL -------

    public static boolean isConnectedToRealms(Minecraft minecraft) {
        //? if >=1.20.2 {
        ServerData server = minecraft.getCurrentServer();
        return server != null && server.isRealm();
        //?} else {
        /*return minecraft.isConnectedToRealms();
        *///?}
    }

    public static void clearLevel(Minecraft minecraft) {
        //? if >=1.21.6 {
        minecraft.disconnectWithProgressScreen();
        //?} elif >=1.20.2 {
        /*minecraft.disconnect();
        *///?} else {
        /*minecraft.clearLevel();
        *///?}
    }

    public static void clearLevel(Minecraft minecraft, Screen progressScreen) {
        //? if >=1.21.6 {
        minecraft.disconnect(progressScreen, false);
        //?} elif >=1.20.2 {
        /*minecraft.disconnect(progressScreen);
        *///?} else {
        /*minecraft.clearLevel(progressScreen);
        *///?}
    }

    public static boolean isLevelTransitionScreen(@Nullable Screen screen) {
        //? if >=1.20.5 {
        if (screen instanceof GenericMessageScreen) return true;
        //?} else {
        /*if (screen instanceof GenericDirtMessageScreen) return true;
        *///?}
        //? if <1.21.9 {
        /*if (screen instanceof ReceivingLevelScreen) return true;
        *///?}
        return screen instanceof ProgressScreen;
    }

    public static Screen savingLevelScreen(Component message) {
        //? if >=1.20.5 {
        return new GenericMessageScreen(message);
        //?} else {
        /*return new GenericDirtMessageScreen(message);
        *///?}
    }

    // ------- SCREENS -------

    @Nullable
    public static Screen screen() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.screen();
        //?} else {
        /*return Minecraft.getInstance().screen;
        *///?}
    }

    public static void setScreen(@Nullable Screen screen) {
        //? if >=26.2 {
        Minecraft.getInstance().gui.setScreen(screen);
        //?} else {
        /*Minecraft.getInstance().setScreen(screen);
        *///?}
    }

    @Nullable
    public static Overlay overlay() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.overlay();
        //?} else {
        /*return Minecraft.getInstance().getOverlay();
        *///?}
    }

    public static void setOverlay(@Nullable Overlay overlay) {
        //? if >=26.2 {
        Minecraft.getInstance().gui.setOverlay(overlay);
        //?} else {
        /*Minecraft.getInstance().setOverlay(overlay);
        *///?}
    }

    // ------- HUD -------

    public static ChatComponent chat() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.hud.getChat();
        //?} else {
        /*return Minecraft.getInstance().gui.getChat();
        *///?}
    }

    public static int guiTicks() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.hud.getGuiTicks();
        //?} else {
        /*return Minecraft.getInstance().gui.getGuiTicks();
        *///?}
    }

    public static boolean isHudHidden() {
        //? if >=26.2 {
        return Minecraft.getInstance().gui.hud.isHidden();
        //?} else {
        /*return Minecraft.getInstance().options.hideGui;
        *///?}
    }

    public static void setHudHidden(boolean hidden) {
        //? if >=26.2 {
        if (Minecraft.getInstance().gui.hud.isHidden() != hidden) {
            Minecraft.getInstance().gui.hud.toggle();
        }
        //?} else {
        /*Minecraft.getInstance().options.hideGui = hidden;
        *///?}
    }

    // ------- WINDOW -------

    public static void resizeDisplay(Minecraft minecraft) {
        //? if >=26.1 {
        minecraft.resizeGui();
        minecraft.gameRenderer.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        //?} else {
        /*minecraft.resizeDisplay();
        *///?}
    }

    public static void updateVsync(Minecraft minecraft) {
        //? if >=26.2 {
        minecraft.invalidateSurfaceConfiguration();
        //?} else {
        /*minecraft.getWindow().updateVsync(minecraft.options.enableVsync().get());
        *///?}
    }

    // ------- CAMERA -------

    public static Vec3 cameraPosition(Camera camera) {
        //? if >=1.21.11 {
        return camera.position();
        //?} else {
        /*return camera.getPosition();
        *///?}
    }

    public static Entity cameraEntity(Camera camera) {
        //? if >=1.21.11 {
        return camera.entity();
        //?} else {
        /*return camera.getEntity();
        *///?}
    }

    // ------- INTERACTION -------

    // 1.20.5 replaced MultiPlayerGameMode.getPickRange() with the interaction range attributes
    public static double blockPickRange(MultiPlayerGameMode gameMode, Player player) {
        //? if >=1.20.5 {
        return player.blockInteractionRange();
        //?} else {
        /*return gameMode.getPickRange();
        *///?}
    }

    // ------- NETWORK -------

    public static ServerboundMovePlayerPacket.Rot rotationPacket(Player player, float yRot, float xRot) {
        //? if >=1.21.2 {
        return new ServerboundMovePlayerPacket.Rot(yRot, xRot, player.onGround(), player.horizontalCollision);
        //?} else {
        /*return new ServerboundMovePlayerPacket.Rot(yRot, xRot, player.onGround());
        *///?}
    }

    // ------- THREADING -------

    public static void schedule(Runnable task) {
        //? if >=1.21.2 {
        Minecraft.getInstance().schedule(task);
        //?} else {
        /*Minecraft.getInstance().tell(task);
        *///?}
    }

    // ------- PROFILER -------

    public static ProfilerFiller profiler() {
        //? if >=1.21.2 {
        return Profiler.get();
        //?} else {
        /*return Minecraft.getInstance().getProfiler();
        *///?}
    }
}

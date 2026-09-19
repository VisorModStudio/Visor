package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
//? if >=1.20.5 {
import net.minecraft.client.gui.screens.GenericMessageScreen;
//?} else {
/*import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
*///?}
//? if <1.21.9 {
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
//?}
//? if >=1.21.2 {
import net.minecraft.util.profiling.Profiler;
//?}

/**
 * Cross-mc-version Utils for client methods
 *
 */
public class McVersionClientUtils {
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
        if (screen instanceof ReceivingLevelScreen) return true;
        //?}
        return screen instanceof ProgressScreen;
    }

    public static Screen savingLevelScreen(Component message) {
        //? if >=1.20.5 {
        return new GenericMessageScreen(message);
        //?} else {
        /*return new GenericDirtMessageScreen(message);
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

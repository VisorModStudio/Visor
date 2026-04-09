package org.vmstudio.visor.api.server;


import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


public class VRServerSettings {
    @Getter
    private static boolean serverDebug = false;
    @Getter
    private static boolean vrOnly = false;

    @Getter
    private static boolean twoHandedVR = true;

    @Getter
    private static boolean roomCrawlingSupported = true;

    @Getter
    private static boolean roomClimbingSupported = true;

    @Getter
    private static boolean pvpVRvsVanilla = true;
    @Getter
    private static boolean pvpVRvsVR = true;
    @Getter
    private static boolean notifyPvpBlocked = false;

    @Getter
    private static double creeperSwellDistance = 1.75;

    @Getter
    private static SupportedMovement supportedMovement = SupportedMovement.BOTH;


    @Getter
    protected static int teleportUpLimit = 1;
    @Getter
    protected static int teleportDownLimit = 4;
    @Getter
    protected static int teleportForwardLimit = 16;


    /**
     * Reset server settings for client when joined dedicated server.
     * <p>
     *     In such case, we configure settings to make
     *     them work on non-visor server.
     *     If server supports visor,
     *     it will send his configuration during handshake
     * </p>
     */
    @Environment(EnvType.CLIENT)
    public static void joinedDedicatedServer(){
        vrOnly = false;
        serverDebug = false;
        roomCrawlingSupported = false;
        roomClimbingSupported = false;
        pvpVRvsVanilla = true;
        pvpVRvsVR = true;
        notifyPvpBlocked = false;
        twoHandedVR = true;
        creeperSwellDistance = 1.75;
        supportedMovement = SupportedMovement.CONTROLLER;
    }
}

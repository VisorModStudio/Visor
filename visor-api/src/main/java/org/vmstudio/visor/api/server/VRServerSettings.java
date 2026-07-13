package org.vmstudio.visor.api.server;


import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


public class VRServerSettings {
    @Getter @SendSettingToClient
    private static boolean serverDebug = false;
    @Getter @SendSettingToClient
    private static boolean vrOnly = false;

    @Getter @SendSettingToClient
    private static boolean twoHandedVR = true;
    @Getter @SendSettingToClient
    private static boolean betterSwinging = true;

    @Getter
    private static long swingingRepairDelay = 400;

    @Getter @SendSettingToClient
    private static boolean roomCrawlingSupported = true;

    @Getter @SendSettingToClient
    private static boolean roomClimbingSupported = true;

    @Getter @SendSettingToClient
    private static boolean pvpVRvsVanilla = true;
    @Getter @SendSettingToClient
    private static boolean pvpVRvsVR = true;
    @Getter @SendSettingToClient
    private static boolean notifyPvpBlocked = false;

    @Getter @SendSettingToClient
    private static double creeperSwellDistance = 1.75;

    @Getter @SendSettingToClient
    private static SupportedMovement supportedMovement = SupportedMovement.BOTH;


    @Getter @SendSettingToClient
    protected static int teleportUpLimit = 1;
    @Getter @SendSettingToClient
    protected static int teleportDownLimit = 4;
    @Getter @SendSettingToClient
    protected static int teleportForwardLimit = 16;

    @Getter @SendSettingToClient
    private static boolean trackersSupported = true;


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
        twoHandedVR = false;
        betterSwinging = false;
        creeperSwellDistance = 1.75;
        supportedMovement = SupportedMovement.CONTROLLER;
        trackersSupported = false;


    }
}

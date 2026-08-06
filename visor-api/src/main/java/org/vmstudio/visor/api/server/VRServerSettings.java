package org.vmstudio.visor.api.server;


import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


public class VRServerSettings {
    @Getter @SendSettingToClient
    private static boolean serverDebug;
    @Getter @SendSettingToClient
    private static boolean vrOnly;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "two_handed")
    private static boolean twoHandedVR;
    @Getter @SendSettingToClient
    @VRServerOptionField(key = "better_swinging")
    private static boolean betterSwinging;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "roomscale_shield")
    @ConfigComment("Block with a raised shield without holding use")
    private static boolean roomscaleShieldBlocking;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "attacks_while_blocking")
    private static boolean attacksWhileBlocking;

    @Getter
    private static long swingingRepairDelay;


    @Getter
    private static float swingingMiningSpeed;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "room_crawling")
    private static boolean roomCrawlingSupported;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "room_climbing")
    private static boolean roomClimbingSupported;

    @Getter @SendSettingToClient
    private static boolean pvpVRvsVanilla;
    @Getter @SendSettingToClient
    private static boolean pvpVRvsVR;
    @Getter @SendSettingToClient
    private static boolean notifyPvpBlocked;

    @Getter @SendSettingToClient
    private static double creeperSwellDistance;

    @Getter @SendSettingToClient
    @VRServerOptionField(key = "supported_movement")
    @ConfigComment("Movement allowed for VR players: CONTROLLER, TELEPORT or BOTH")
    private static SupportedMovement supportedMovement;


    @Getter @SendSettingToClient
    @ConfigComment("Teleport distance limits in blocks")
    protected static int teleportUpLimit;
    @Getter @SendSettingToClient
    protected static int teleportDownLimit;
    @Getter @SendSettingToClient
    protected static int teleportForwardLimit;

    @Getter @SendSettingToClient
    @ConfigComment({"[TRACKERS] - Potentially, more network consumption when enabled",})
    private static boolean bodyTrackersSupported;

    @Getter @SendSettingToClient
    private static boolean handTrackersSupported;

    @Getter
    @ConfigComment("Send body tracker data to other players (if enabled, players with Visor mod and within VR player visibility range receive such data)")
    private static boolean bodyTrackersSendToOthers;

    @Getter
    @ConfigComment("Send hand tracking data to other players (if enabled, players with Visor mod and within VR player visibility range receive such data)")
    private static boolean handTrackersSendToOthers;

    static {
        resetToDefaults();
    }

    public static void resetToDefaults(){
        serverDebug = false;
        vrOnly = false;
        twoHandedVR = true;
        betterSwinging = true;
        roomscaleShieldBlocking = true;
        attacksWhileBlocking = true;
        swingingRepairDelay = 400;
        swingingMiningSpeed = 1.5f;
        roomCrawlingSupported = true;
        roomClimbingSupported = true;
        pvpVRvsVanilla = true;
        pvpVRvsVR = true;
        notifyPvpBlocked = false;
        creeperSwellDistance = 1.75;
        supportedMovement = SupportedMovement.BOTH;
        teleportUpLimit = 1;
        teleportDownLimit = 4;
        teleportForwardLimit = 16;
        bodyTrackersSupported = true;
        handTrackersSupported = true;
        bodyTrackersSendToOthers = true;
        handTrackersSendToOthers = true;
    }


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
        roomscaleShieldBlocking = false;
        attacksWhileBlocking = true;
        creeperSwellDistance = 1.75;
        supportedMovement = SupportedMovement.CONTROLLER;
        bodyTrackersSupported = false;
        handTrackersSupported = false;


    }
}

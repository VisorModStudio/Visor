package me.phoenixra.visor.api.server;


import lombok.Getter;
import lombok.Setter;


public class VRServerSettings {
    @Getter @Setter
    private static boolean serverDebug = false;
    @Getter @Setter
    private static boolean vrOnly = false;

    @Getter @Setter
    private static boolean pvpVRvsVanilla = true;
    @Getter @Setter
    private static boolean pvpVRvsVR = true;
    @Getter @Setter
    private static boolean notifyPvpBlocked = false;

    @Getter @Setter
    private static double creeperSwellDistance = 1.75;

}

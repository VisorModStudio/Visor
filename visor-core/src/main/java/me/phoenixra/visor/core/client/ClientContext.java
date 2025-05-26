package me.phoenixra.visor.core.client;

import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.data.raw.RawPlayerPose;
import me.phoenixra.visor.core.client.gui.VRGuiManager;
import me.phoenixra.visor.core.client.render.VisorRenderer;
import me.phoenixra.visor.core.client.render.gameview.VRGameViewHandler;
import me.phoenixra.visor.core.client.render.gameview.hand.VRHandRenderer;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.common.addon.VRAddonClientCore;

/**
 * Main purpose of this class is to hold instances of
 * important classes, that are responsible for major CLIENT-SIDE mod parts,
 * to reduce the number of method calls
 */
public class ClientContext {


    public static VisorClient visor;

    public static VRAddonClientCore coreAddon;

    public static ClientProperties properties;

    public static VRClientSettingsHandler settingsHandler;

    public static VRClientPlayer player;

    public static VisorRenderer renderer;


    public static VRGuiManager guiManager;



    public static RawPlayerPose rawPlayerPose;

    public static VRGameViewHandler gameViewHandler;
    public static VRHandRenderer handRenderer;



}

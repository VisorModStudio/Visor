package me.phoenixra.visor.core.client;

import me.phoenixra.visor.core.client.data.VRClientPlayerImpl;
import me.phoenixra.visor.core.client.data.raw.RawPoseHandler;
import me.phoenixra.visor.core.client.gui.VRCursorHandlerImpl;
import me.phoenixra.visor.core.client.gui.VRGuiManagerImpl;
import me.phoenixra.visor.core.client.gui.VROverlayManagerImpl;
import me.phoenixra.visor.core.client.input.InputManagerImpl;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.decoration.DecorationRendererImpl;
import me.phoenixra.visor.core.client.render.decoration.hand.VRHandRenderer;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.common.addon.AddonManagerImpl;
import me.phoenixra.visor.core.common.addon.CoreAddonClient;

/**
 * Main purpose of this class is to hold instances of
 * important classes, that are responsible for major CLIENT-SIDE mod parts,
 * to reduce the number of method calls
 */
public class ClientContext {


    public static VisorClientImpl visor;

    public static AddonManagerImpl addonManager;

    public static CoreAddonClient coreAddon;

    public static VRClientSettingsHandler settingsHandler;

    public static VRClientPlayerImpl player;

    public static VisorRendererBase renderer;


    public static InputManagerImpl inputManager;

    public static VRGuiManagerImpl guiManager;
    public static VRCursorHandlerImpl cursorHandler;
    public static VROverlayManagerImpl overlayManager;



    public static RawPoseHandler rawPoseHandler;

    public static DecorationRendererImpl decorationRenderer;
    public static VRHandRenderer handRenderer;



}

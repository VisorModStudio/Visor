package me.phoenixra.visor.core.client;

import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.data.raw.RawPoseHandler;
import me.phoenixra.visor.core.client.gui.VRCursorHandlerImpl;
import me.phoenixra.visor.core.client.gui.GuiManagerImpl;
import me.phoenixra.visor.core.client.gui.OverlayManagerImpl;
import me.phoenixra.visor.core.client.input.InputManagerImpl;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import me.phoenixra.visor.core.client.render.decoration.DecoratorManagerImpl;
import me.phoenixra.visor.core.client.render.decoration.hand.VRHandRendererImpl;
import me.phoenixra.visor.core.client.settings.VRClientSettingsHandler;
import me.phoenixra.visor.core.common.addon.AddonCoreClient;

/**
 * Main purpose of this class is to hold instances of
 * important classes, that are responsible for major CLIENT-SIDE mod parts,
 * to reduce the number of method calls
 */
public class ClientContext {


    public static VisorClientImpl visor;

    public static AddonCoreClient coreAddon;

    public static VRClientSettingsHandler settingsHandler;

    public static VRClientPlayer player;

    public static VisorRendererBase renderer;


    public static InputManagerImpl inputManager;

    public static GuiManagerImpl guiManager;
    public static VRCursorHandlerImpl cursorHandler;
    public static OverlayManagerImpl overlayManager;



    public static RawPoseHandler rawPoseHandler;

    public static DecoratorManagerImpl decoratorManager;
    public static VRHandRendererImpl handRenderer;



}

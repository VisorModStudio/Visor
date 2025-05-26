package me.phoenixra.visor.core.client;

import me.phoenixra.visor.core.client.data.VRClientPlayer;
import me.phoenixra.visor.core.client.data.raw.RawPoseHandler;
import me.phoenixra.visor.core.client.gui.GuiManagerImpl;
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

    public static ClientPropertiesImpl properties;

    public static VRClientSettingsHandler settingsHandler;

    public static VRClientPlayer player;

    public static VisorRendererBase renderer;


    public static GuiManagerImpl guiManager;



    public static RawPoseHandler rawPoseHandler;

    public static DecoratorManagerImpl decoratorManager;
    public static VRHandRendererImpl handRenderer;



}

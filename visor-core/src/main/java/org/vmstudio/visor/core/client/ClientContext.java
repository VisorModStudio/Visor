package org.vmstudio.visor.core.client;

import me.phoenixra.atumvr.api.input.AtumVRInputHandler;
import org.vmstudio.visor.core.client.player.VRLocalPlayerImpl;
import org.vmstudio.visor.core.client.player.pose.raw.RawPoseHandler;
import org.vmstudio.visor.core.client.gui.VRCursorHandlerImpl;
import org.vmstudio.visor.core.client.gui.VRGuiManagerImpl;
import org.vmstudio.visor.core.client.gui.VROverlayManagerImpl;
import org.vmstudio.visor.core.client.input.VRInputManagerImpl;
import org.vmstudio.visor.core.client.render.VRRendererBase;
import org.vmstudio.visor.core.client.render.decoration.DecorationRendererImpl;
import org.vmstudio.visor.core.client.render.decoration.hand.VRHandRenderer;
import org.vmstudio.visor.core.client.settings.VRClientSettingsManager;
import org.vmstudio.visor.core.common.addon.AddonManagerImpl;
import org.vmstudio.visor.core.common.addon.CoreAddonClient;

/**
 * Main purpose of this class is to hold instances of
 * important classes, that are responsible for major CLIENT-SIDE mod parts,
 * to reduce the number of method calls
 */
public class ClientContext {


    public static VisorClientImpl visor;

    public static AddonManagerImpl addonManager;

    public static CoreAddonClient coreAddon;

    public static VRClientSettingsManager settingsManager;

    public static VRLocalPlayerImpl localPlayer;

    public static VRRendererBase renderer;


    public static VRInputManagerImpl inputManager;

    /** Input handler from VR provider */
    public static AtumVRInputHandler inputProvider;

    public static VRGuiManagerImpl guiManager;
    public static VRCursorHandlerImpl cursorHandler;
    public static VROverlayManagerImpl overlayManager;



    public static RawPoseHandler rawPoseHandler;

    public static DecorationRendererImpl decorationRenderer;
    public static VRHandRenderer handRenderer;



}

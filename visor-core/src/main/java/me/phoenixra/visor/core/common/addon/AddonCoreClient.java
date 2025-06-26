package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//Core Addon for client
public class AddonCoreClient implements VisorAddon {
    public AddonCoreClient(){
        ClientContext.coreAddon = this;
    }
    @Override
    public void onAddonLoad() {
        ClientContext.overlayManager.getOverlaysRegistry()
                .registerElements(
                        List.of(
                                new VROverlayGameScreen(
                                        ClientContext.coreAddon,
                                        VROverlayGameScreen.ID
                                ),
                                new VROverlayHotBar(
                                        ClientContext.coreAddon,
                                        ControllerHand.MAIN,
                                        VROverlayHotBar.ID_MAIN
                                ),
                                new VROverlayHotBar(
                                        ClientContext.coreAddon,
                                        ControllerHand.OFFHAND,
                                        VROverlayHotBar.ID_OFFHAND
                                ),
                                new VROverlayKeyboard(
                                        ClientContext.coreAddon,
                                        VROverlayKeyboard.ID
                                ),
                                new VROverlaySettings(
                                        ClientContext.coreAddon,
                                        VROverlaySettings.ID
                                ),
                                new VROverlayOptionsMenu(
                                        ClientContext.coreAddon,
                                        VROverlayOptionsMenu.ID
                                ),
                                new VROverlayDemo(
                                        ClientContext.coreAddon,
                                        VROverlayDemo.ID
                                )
                        )
                );
    }




    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core.client";
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }
}

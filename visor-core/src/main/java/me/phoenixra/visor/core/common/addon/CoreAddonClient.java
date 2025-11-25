package me.phoenixra.visor.core.common.addon;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//Core Addon for client
public class CoreAddonClient implements VisorAddon {


    public CoreAddonClient(){
        ClientContext.coreAddon = this;
    }
    @Override
    public void onAddonLoad() {
        ClientContext.overlayManager.getOverlaysRegistry()
                .registerElements(
                        List.of(
                                new VROverlayGameScreen(
                                        this,
                                        VROverlayGameScreen.ID
                                ),
                                new VROverlayThirdPersonCamera(
                                        this,
                                        VROverlayThirdPersonCamera.ID
                                ),
                                new VROverlayHotBar(
                                        this,
                                        HandType.MAIN,
                                        VROverlayHotBar.ID_MAIN
                                ),
                                new VROverlayHotBar(
                                        this,
                                        HandType.OFFHAND,
                                        VROverlayHotBar.ID_OFFHAND
                                ),
                                new VROverlayKeyboard(
                                        this,
                                        VROverlayKeyboard.ID
                                ),
                                new VROverlaySettings(
                                        this,
                                        VROverlaySettings.ID
                                ),
                                new VROverlayOptionsMenu(
                                        this,
                                        VROverlayOptionsMenu.ID
                                ),
                                new VROverlayDemo(
                                        this,
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
    public @NotNull Component getAddonName() {
        return Component.literal("Core");
    }

    @Override
    public @NotNull String getAddonId() {
        return "core";
    }

    @Override
    public GuiTexture getAddonIcon() {
        return VisorAPI.NOD_ICON;
    }

    @Override
    public String getModId() {
        return VisorAPI.MOD_ID;
    }
}

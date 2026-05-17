package org.vmstudio.visor.core.common.addon;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayMovementState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayItemPoseTest;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayThirdPersonCamera;
import org.vmstudio.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlayOptionsMenu;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.network.ClientNetworking;

import java.util.List;

//Core Addon for client
public class CoreAddonClient implements VisorAddon {


    public CoreAddonClient(){
        ClientContext.coreAddon = this;
    }

    @Override
    public void onAddonRegister() {
        ClientNetworking.createClientChannel(this);
    }

    @Override
    public void onAddonLoad() {
        ClientContext.overlayManager.getOverlaysRegistry()
                .registerComponents(
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
                                ),
                                new VROverlayMovementState(
                                        this,
                                        VROverlayMovementState.ID
                                ),
                                new VROverlayItemPoseTest(
                                        this,
                                        VROverlayItemPoseTest.ID
                                )
                        )
                );
    }


    @Override
    public @Nullable String getAddonPackagePath() {
        return "org.vmstudio.visor.core.client";
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

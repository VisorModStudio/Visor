package org.vmstudio.visor.core.common.addon;


import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.server.network.ServerNetworking;

//Core Addon for dedicated server
public class CoreAddonServer implements VisorAddon {


    @Override
    public void onAddonRegister() {
        ServerNetworking.createDedicatedChannel(this);
    }

    @Override
    public void onAddonLoad() {

    }



    @Override
    public @Nullable String getAddonPackagePath() {
        return "org.vmstudio.visor.core.server";
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

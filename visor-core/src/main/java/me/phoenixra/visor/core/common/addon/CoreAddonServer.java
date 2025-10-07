package me.phoenixra.visor.core.common.addon;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Core Addon for dedicated server
public class CoreAddonServer implements VisorAddon {
    @Override
    public void onAddonLoad() {

    }



    @Override
    public @Nullable String getAddonPackagePath() {
        return "me.phoenixra.visor.core.server";
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
